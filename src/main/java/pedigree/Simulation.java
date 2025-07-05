package pedigree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class Simulation {
    private final AgeModel ageModel = new AgeModel();
    private PriorityQueue<Event> eventQ;
    private PriorityQueue<Sim> livingFemales;
    private PriorityQueue<Sim> livingMales;
    private Random rnd;
    private double reproductionRate = 0.1; // valeur par défaut, à ajuster
    private double fidelity = 0.9;

    public Simulation() {
        this.eventQ = new PriorityQueue<>();
        this.livingFemales = new PriorityQueue<>();
        this.livingMales = new PriorityQueue<>();
        this.rnd = new Random();
    }

    public void simulate(int n, double tMax) {
        for (int i = 0; i < n; i++) {
            Sim founder = new Sim(Sim.Sex.getSex());
            scheduleEvent(new Event(founder, Event.Type.BIRTH, 0.0));
        }

        while (!eventQ.isEmpty()) {
            Event e = eventQ.poll();
            if (e.time > tMax) break;
            if (e.time < e.subject.getDeathTime()) {
                handleEvent(e);
            }
        }
    }

    private void scheduleEvent(Event e) {
        eventQ.add(e);
    }

    private void handleEvent(Event e) {
        switch (e.type) {
            case BIRTH -> handleBirth(e);
            case DEATH -> handleDeath(e);
            case REPRODUCTION -> handleReproduction(e);
        }
    }

    private void handleBirth(Event e) {
        Sim sim = e.subject;

        double lifespan = ageModel.randomAge(rnd);
        double deathTime = e.time + lifespan;
        sim.setDeath(deathTime);
        scheduleEvent(new Event(sim, Event.Type.DEATH, deathTime));

        if (sim.getSex() == Sim.Sex.F) {
            double waitingTime = ageModel.randomWaitingTime(rnd, reproductionRate);
            scheduleEvent(new Event(sim, Event.Type.REPRODUCTION, e.time + waitingTime));
            livingFemales.add(sim);
        } else {
            livingMales.add(sim);
        }
    }

    private void handleDeath(Event e) {
        if (e.subject.getSex() == Sim.Sex.F) {
            livingFemales.remove(e.subject);
        } else {
            livingMales.remove(e.subject);
        }
    }

    private void handleReproduction(Event e) {
        Sim mother = e.subject;

        if (!mother.isMatingAge(e.time)) return;

        Sim father = choosePartner(mother, e.time);
        if (father == null) return;

        Sim.Sex childSex = Sim.Sex.getSex();
        Sim child = new Sim(mother, father, e.time, childSex);
        scheduleEvent(new Event(child, Event.Type.BIRTH, e.time));

        mother.setMate(father);
        father.setMate(mother);

        double nextWaiting = AgeModel.randomWaitingTime(rnd, reproductionRate);
        scheduleEvent(new Event(mother, Event.Type.REPRODUCTION, e.time + nextWaiting));
    }

    private Sim choosePartner(Sim mother, double time) {
        if (mother.isInARelationship(time) && rnd.nextDouble() < fidelity) {
            return mother.getMate();
        }

        List<Sim> candidates = new ArrayList<>(livingMales);
        Collections.shuffle(candidates);
        for (Sim candidate : candidates) {
            if (candidate.isMatingAge(time)) {
                if (!candidate.isInARelationship(time) || rnd.nextDouble() > fidelity) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static class Event implements Comparable<Event> {
        public enum Type { BIRTH, DEATH, REPRODUCTION }

        Sim subject;
        Type type;
        double time;

        public Event(Sim subject, Type type, double time) {
            this.subject = subject;
            this.type = type;
            this.time = time;
        }

        @Override
        public int compareTo(Event o) {
            return Double.compare(this.time, o.time);
        }
    }
}