package pedigree;


import java.util.*;

import pedigree.Sim.*;

public class Simulator {
    private PriorityQueue<Event> events;
    private PriorityQueue<Sim> males;
    private PriorityQueue<Sim> females;
    private final AgeModel model;
    private double calendarTime;
    private final Random rnd;
    private final double fidelity = 0.1;
    private final double span;

    static class EventComparator implements Comparator<Event>{
        @Override
        public int compare(Event o1, Event o2) {
            return Double.compare(o1.getTime(),o2.getTime());
        }
    }

    static class PopComparator implements Comparator<Sim>{
        @Override
        public int compare(Sim o1, Sim o2) {
            return Double.compare(o1.getBirthTime(),o2.getBirthTime());
        }
    }
    public Simulator() {
        events = new PriorityQueue<>(new EventComparator());
        males = new PriorityQueue<>(new PopComparator());
        females = new PriorityQueue<>(new PopComparator());
        model = new AgeModel();
        span = model.expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
        rnd = new Random();
        calendarTime = 0.0;
    }

    public double getTime(){
        return calendarTime;
    }

    public void setTime(double time){
        calendarTime = time;
    }

    public int getPopulation(){
        return males.size() + females.size();
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
        if (founder.getSex().equals(Sex.F)){
            females.add(founder);
            double firstReproduction = founder.getBirthTime() + Sim.MIN_MATING_AGE_F + rnd.nextDouble() * span;
            events.add(new Event(Events.Reproduction, founder, firstReproduction));
        }
        else {
            males.add(founder);
        }
        double death = founder.getBirthTime() + model.randomAge(rnd);
        founder.setDeath(calendarTime + death);
        events.add(new Event(Events.Death, founder, founder.getDeathTime()));
    }

    public void Birth(Sim mother, Sim father){
        Sex sex = Sex.getSex();
        Sim child = new Sim(mother, father, calendarTime, sex);
        if (child.getSex().equals(Sex.F)){
            females.add(child);
            double firstReproduction = child.getBirthTime() + Sim.MIN_MATING_AGE_F + rnd.nextDouble() * span;
            events.add(new Event(Events.Reproduction, child, firstReproduction));
        }
        else {
            males.add(child);
        }
        double death = child.getBirthTime() + model.randomAge(rnd);
        child.setDeath(calendarTime + death);
        events.add(new Event(Events.Death, child, child.getDeathTime()));
    }

    public void Death(Sim s){
        if (s.getSex().equals(Sex.F)) {
            females.remove(s);
        }
        else {
            males.remove(s);
        }
    }

    public boolean isFaithful(){
        return rnd.nextDouble() > fidelity;
    }

    public Sim Mate(Sim f){
        if (f.isInARelationship(calendarTime) && isFaithful()){
            return f.getMate();
        }
        if (males.isEmpty()){
            return null;
        }
        List<Sim> availableMales = new ArrayList<>();
        for (Sim m : males){
            if (m.isMatingAge(calendarTime)){
                if(!m.isInARelationship(calendarTime) || !isFaithful()){
                    availableMales.add(m);
                }
            }
        }
        if (availableMales.isEmpty()){
            return null;
        }

        Sim male = availableMales.get(rnd.nextInt(availableMales.size()));

        f.setMate(male);
        male.setMate(f);

        return f.getMate();
    }

    private double nextReproduction(Sim s){
        double age = calendarTime - s.getBirthTime();
        if (age < Sim.MIN_MATING_AGE_F){
            double wait = Sim.MIN_MATING_AGE_F - age;
            return calendarTime + wait;
        }
        double maxReproduction = s.getBirthTime() + Sim.MAX_MATING_AGE_F;
        double nextTime = calendarTime + span * rnd.nextDouble();
        if (nextTime < maxReproduction) { return nextTime; }
        else { return -1; }
    }
    public void Reproduction(Sim mother){
        if (mother.getDeathTime() < calendarTime){
            return;
        }
        if (mother.isMatingAge(calendarTime)) {
            Sim father = Mate(mother);
            if (father != null) {
                Birth(mother, father);
            }
        }
        double nextTime = nextReproduction(mother);
        if (nextTime > 0 && nextTime < mother.getDeathTime() &&
                calendarTime - mother.getBirthTime() < Sim.MAX_MATING_AGE_F) {
            events.add(new Event(Events.Reproduction, mother, nextTime));
        }
    }
}
