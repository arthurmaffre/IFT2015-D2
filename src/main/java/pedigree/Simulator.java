package pedigree;


import java.awt.*;
import java.util.PriorityQueue;
import java.util.Random;

import pedigree.Sim.*;

public class Simulator {
    private PriorityQueue<Event> events;
    private PriorityQueue<Sim> population;
    private final AgeModel model;
    private int time;
    private final Random rnd;
    private final float fidelity = 0.1F;

    public Simulator() {
        events = new PriorityQueue<>();
        population = new PriorityQueue<>();
        model = new AgeModel();
        rnd = new Random();
    }

    public void Birth(Sim mother, Sim father){
        Sex sex = Sex.getSex();
        Sim child = new Sim(mother, father, time, sex);
        double death = model.randomAge(rnd);
        child.setDeath(death);
        population.add(child);
    }

    public void Death(Sim s){
        population.remove(s);
    }

    public boolean isFaithful(Sim s){
        return rnd.nextFloat() > fidelity;
    }

    public Sim Mate(Sim f){
        if (f.isInARelationship(time) && isFaithful(f.getMate())){
            return f.getMate();
        }
        else{
            //todo: expensive filtering operation, maintain male and female queues?
            Object[] males = population.stream().filter(p -> p.getSex() == Sex.M).toArray();
            Sim mate = (Sim)males[rnd.nextInt(males.length)];
            f.setMate(mate);
        }
        return f.getMate();
    }

    public void Reproduction(Sim mother){
        if (mother.getDeathTime()> time){
            Death(mother);
            return;
        }
        Sim father = Mate(mother);
        Birth(mother, father);
    }
}
