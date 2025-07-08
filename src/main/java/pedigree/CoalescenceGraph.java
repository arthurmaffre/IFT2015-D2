package pedigree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoalescenceGraph {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: CoalescenceGraph <n> <tMax>");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        double tMax = Double.parseDouble(args[1]);

        Simulator simulator = new Simulator();
        for (int i=0;i<n;i++) {
            Sim founder = new Sim(Sim.Sex.getSex());
            simulator.Birth(founder);
        }

        double nextSample = 0.0;
        List<CoalescenceAnalyzer.Point> pop = new ArrayList<>();

        while (simulator.hasEvents()) {
            Simulator.Event E = simulator.getEvent();
            if (E.getTime() > tMax) break;
            simulator.setTime(E.getTime());

            if (simulator.getTime() >= nextSample) {
                pop.add(new CoalescenceAnalyzer.Point(simulator.getTime(), simulator.getPopulation()));
                nextSample += 100.0;
            }

            if (E.getTime() < E.getSim().getDeathTime()) {
                switch (E.getEvent()) {
                    case Birth -> simulator.Birth(E.getSim());
                    case Death -> simulator.Death(E.getSim());
                    case Reproduction -> simulator.Reproduction(E.getSim());
                    case EntersMatingAge -> simulator.EntersMatingAge(E.getSim());
                    case ExitsMatingAge -> simulator.ExitsMatingAge(E.getSim());
                }
            }
        }

        List<CoalescenceAnalyzer.Point> maternal = CoalescenceAnalyzer.maternal(simulator.getFemales());
        List<CoalescenceAnalyzer.Point> paternal = CoalescenceAnalyzer.paternal(simulator.getMales());

        SimpleSVG.write("coalescence.svg", pop, maternal, paternal);
        System.out.println("Graph written to coalescence.svg");
    }
}
