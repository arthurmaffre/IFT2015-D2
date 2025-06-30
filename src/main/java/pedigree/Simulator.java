package pedigree;


import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import pedigree.Sim.*;

public class Simulator {
    private PriorityQueue<Event> events;
    private PriorityQueue<Sim> population;
    private final AgeModel model;
    private double calendarTime;
    private final Random rnd;
    private final float fidelity = 0.1F;

    static class EventComparator implements Comparator<Event>{
        @Override
        public int compare(Event o1, Event o2) {
            return Double.compare(o1.time,o2.time);
        }
    }
    public Simulator() {
        events = new PriorityQueue<>(new EventComparator());
        population = new PriorityQueue<>();
        model = new AgeModel();
        rnd = new Random();
    }

    public double getTime(){
        return calendarTime;
    }

    public void setTime(double time){
        calendarTime = time;
    }
    public enum Events {Birth, Death, Reproduction};

    public class Event {
        private Events event;
        private Sim sim;
        private double time;

        public Event(Events e, Sim s, double t) {
            event = e; sim = s; time = t;
            }

        public Events getEvent(){
            return event;
        }

        public Sim getSim(){
            return sim;
        }

        public double getTime(){
            return time;
        }
    }

    public Event getEvent() {
        return events.poll();
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public void Birth(Sim founder){
        population.add(founder);
        if (founder.getSex().equals(Sex.F)){
            events.add(new Event(Events.Reproduction, founder, founder.getBirthTime()));
        }
        double death = model.randomAge(rnd);
        founder.setDeath(death);
        events.add(new Event(Events.Death, founder, founder.getDeathTime()));
    }

    public void Birth(Sim mother, Sim father){
        Sex sex = Sex.getSex();
        Sim child = new Sim(mother, father, calendarTime, sex);
        population.add(child);
        if (child.getSex().equals(Sex.F)){
            events.add(new Event(Events.Reproduction, child, child.getBirthTime()));
        }
        double death = model.randomAge(rnd);
        child.setDeath(death);
        events.add(new Event(Events.Death, child, child.getDeathTime()));
    }

    public void Death(Sim s){
        population.remove(s);
    }

    public boolean isFaithful(){
        return rnd.nextFloat() > fidelity;
    }

    public Sim Mate(Sim f){
        if (f.isInARelationship(calendarTime) && isFaithful()){
            return f.getMate();
        }
        else{
            //todo: expensive filtering operation, maintain male and female queues?
            Object[] males = population.stream().filter(p -> p.getSex() == Sex.M && p.isMatingAge(calendarTime)).toArray();
            Sim mate = (Sim)males[rnd.nextInt(males.length)];
            f.setMate(mate);
        }
        return f.getMate();
    }

    private double nextReproduction(Sim s){
        double age = calendarTime - s.getBirthTime();
        return rnd.nextDouble(model.expectedParenthoodSpan(age, Sim.MAX_MATING_AGE_F));
    }
    public void Reproduction(Sim mother){
        if (mother.getDeathTime()> calendarTime){
            Death(mother);
        }
        else if (mother.isMatingAge(calendarTime)) {
            Sim father = Mate(mother);
            Birth(mother, father);
        }
        events.add(new Event(Events.Reproduction, mother, nextReproduction(mother)));
    }
}
