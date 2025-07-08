package pedigree;


import java.util.*;

import pedigree.Sim.*;

public class Simulator {
    private PriorityQueue<Event> events;
    private PriorityQueue<Sim> males;
    private Set<Sim> availableMales;
    private PriorityQueue<Sim> females;
    private final AgeModel model;
    private double calendarTime;
    private final Random rnd;
    private final double fidelity;
    private final double span;
    private final double reproductionRate;

    private static final double DEFAULT_FIDELITY = 0.1;
    private static final double DEFAULT_STABLE_RATE = 2.2;

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
        availableMales = new TreeSet<>(new PopComparator());
        females = new PriorityQueue<>(new PopComparator());
        model = new AgeModel();
        span = model.expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
        fidelity = DEFAULT_FIDELITY;
        reproductionRate = DEFAULT_STABLE_RATE/span;
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

    /**
     * Living females at the current simulation time.
     *
     * @return unmodifiable view of all alive females
     */
    public Collection<Sim> getFemales(){
        return Collections.unmodifiableCollection(females);
    }

    /**
     * Living males at the current simulation time.
     *
     * @return unmodifiable view of all alive males
     */
    public Collection<Sim> getMales(){
        return Collections.unmodifiableCollection(males);
    }

    public enum Events {Birth, Death, Reproduction, EntersMatingAge, ExitsMatingAge};

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
            double firstReproduction = founder.getBirthTime() + Sim.MIN_MATING_AGE_F + AgeModel.randomWaitingTime(rnd, reproductionRate);
            events.add(new Event(Events.Reproduction, founder, firstReproduction));
        }
        else {
            males.add(founder);
            events.add(new Event(Events.EntersMatingAge, founder, founder.getBirthTime() + Sim.MIN_MATING_AGE_M));
            events.add(new Event(Events.ExitsMatingAge, founder, founder.getBirthTime() + Sim.MAX_MATING_AGE_M));
        }
        double death = founder.getBirthTime() + model.randomAge(rnd);
        founder.setDeath(death);
        events.add(new Event(Events.Death, founder, founder.getDeathTime()));
    }

    public void Birth(Sim mother, Sim father){
        Sex sex = Sex.getSex();
        Sim child = new Sim(mother, father, calendarTime, sex);
        if (child.getSex().equals(Sex.F)){
            females.add(child);
            double firstReproduction = child.getBirthTime() + Sim.MIN_MATING_AGE_F + AgeModel.randomWaitingTime(rnd, reproductionRate);
            events.add(new Event(Events.Reproduction, child, firstReproduction));
        }
        else {
            males.add(child);
            events.add(new Event(Events.EntersMatingAge, child, child.getBirthTime() + Sim.MIN_MATING_AGE_M));
            events.add(new Event(Events.ExitsMatingAge, child, child.getBirthTime() + Sim.MAX_MATING_AGE_M));
        }
        double death = child.getBirthTime() + model.randomAge(rnd);
        child.setDeath(death);
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

    public void EntersMatingAge(Sim male) {
        availableMales.add(male);
    }

    public void ExitsMatingAge(Sim male) {
        availableMales.remove(male);
    }

    public Sim Mate(Sim f){
        if (f.isInARelationship(calendarTime) && isFaithful()){
            return f.getMate();
        }
        if (availableMales.isEmpty()){
            return null;
        }

        List<Sim> availableList = new ArrayList<>(availableMales);
        Sim male = availableList.get(rnd.nextInt(availableList.size()));

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
        double wait = AgeModel.randomWaitingTime(rnd, reproductionRate);
        double nextTime = calendarTime + wait;
        if (nextTime < maxReproduction) {
            return nextTime;
        } else {
            return -1;
        }
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
