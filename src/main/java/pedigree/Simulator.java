package pedigree;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import pedigree.Sim.Sex;

public class Simulator {
    private PriorityQueueO<Event> events;
    private PriorityQueueO<Sim> males;
    private Set<Sim> availableMales;
    private PriorityQueueO<Sim> females;
    private final AgeModel model;
    private double calendarTime;
    private final Random rnd;
    private final double fidelity;
    private final double span;
    private final double reproductionRate;
    private final double horizon;

    private double nextSample;
    private final List<PointPop> popSamples;

    private static final double DEFAULT_FIDELITY = 0.1;
    private static final double DEFAULT_STABLE_RATE = 2.0;

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
    public Simulator(long seed, double horizon) {
        events = new PriorityQueueO<>(new EventComparator());
        males = new PriorityQueueO<>(new PopComparator());
        availableMales = new TreeSet<>(new PopComparator());
        females = new PriorityQueueO<>(new PopComparator());
        model = new AgeModel();
        span = model.expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
        fidelity = DEFAULT_FIDELITY;
        reproductionRate = DEFAULT_STABLE_RATE / span;
        rnd = new Random(seed);
        calendarTime = 0.0; // Starting Time
        this.horizon = horizon;
        nextSample = 0.0;
        popSamples = new ArrayList<>();
    }

    public Simulator(long seed) {
        this(seed, 0.0);
    }

    public Simulator() {
        this(System.currentTimeMillis(), 0.0);
    }

    public double getTime(){
        return calendarTime;
    }

    public void setTime(double time){
        calendarTime = time;
    }

    public int getPopulation(){ // Donne la population
        return males.size() + females.size();
    }

    public enum Events {Birth, Death, Reproduction, EntersMatingAge, ExitsMatingAge};

    /** Point de suivi de la population vivante. */
    public record PointPop(double time, int pop) {}








    // Classe de Event
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

    /** Ajoute un événement de naissance sans exécution immédiate. */
    public void scheduleBirthEvent(Sim sim) {
        events.add(new Event(Events.Birth, sim, sim.getBirthTime()));
    }




    // Naissance
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
        samplePopulation();
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
        samplePopulation();
    }





    // Mort
    public void Death(Sim s){
        if (s.getSex().equals(Sex.F)) {
            females.remove(s);
        }
        else {
            males.remove(s);
        }
        samplePopulation();
    }


    // Reproduction

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
        samplePopulation();
    }

    /** Renvoie la population vivante à l'instant courant. */
    public Collection<Sim> getLivingPopulation() {
        List<Sim> pop = new ArrayList<>(males);
        pop.addAll(females);
        return pop;
    }

    /** Échantillons de population tous les 100 ans. */
    public List<PointPop> getPopSamples() {
        return popSamples;
    }

    /** Force l'enregistrement d'un échantillon à l'instant courant. */
    public void recordSample() {
        samplePopulation();
    }

    /** Met à jour la liste de points si l'on a passé le prochain jalon. */
    private void samplePopulation() {
        while (calendarTime >= nextSample && nextSample <= horizon) {
            popSamples.add(new PointPop(nextSample, getPopulation()));
            nextSample += 100.0;
        }
    }
}