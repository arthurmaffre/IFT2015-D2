package pedigree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

/**
 * Coalescence : retrace, en remontant le temps, le nombre de lignées paternelles
 * (chromosome Y) et maternelles (ADN mt) représentées dans la population
 * vivante à un instant donné.
 * <p>Algorithme § 2.3 : tas « plus jeune d’abord » + Set pour tester la fusion.
 */
public final class Coalescence {

    /** Point (t, n) : date (année < 0 passé, 0 présent) et nombre de lignées. */
    public record Point(double time, int lineages) {}

    /* ===================== Public façade ===================== */

    /** Lignées paternelles. */
    public static List<Point> paternal(Simulator sim, double tMax) {
        return compute(sim.getLivingPopulation(), Sim::getFather, tMax);
    }

    /** Lignées maternelles. */
    public static List<Point> maternal(Simulator sim, double tMax) {
        return compute(sim.getLivingPopulation(), Sim::getMother, tMax);
    }

    /* ====================== Cœur générique ====================== */

    private static List<Point> compute(Collection<Sim> pop,
                                       Function<Sim, Sim> parentSel,
                                       double tMax) {
        // Tas trié « plus jeune d’abord » (birthTime décroissant)
        PriorityQueueO<Sim> pq = new PriorityQueueO<>((a,b) -> Double.compare(b.getBirthTime(), a.getBirthTime()));
        pq.addAll(pop);

        HashSet<Sim> active = new HashSet<>(pop);
        List<Point> traj = new ArrayList<>();
        int n = active.size();
        if (!pq.isEmpty()) traj.add(new Point(pq.peek().getBirthTime(), n));

        while (n > 1 && !pq.isEmpty()) {
            Sim child  = pq.poll();
            Sim parent = parentSel.apply(child);
            if (parent == null) continue;                  // fondateur

            if (!active.add(parent)) {                     // déjà présent → fusion
                n--;  traj.add(new Point(child.getBirthTime(), n));
            } else {
                pq.add(parent);                           // nouvelle lignée à explorer
            }

            if (tMax > 0 && child.getBirthTime() - parent.getBirthTime() > tMax)
                break;                                     // profondeur max
        }
        return traj;
    }

    /* =================== Petit exécutable CLI =================== */

    public static void main(String[] args) {
        int founders = args.length>0 ? Integer.parseInt(args[0]) : 1000;
        double horizon = args.length>1 ? Double.parseDouble(args[1]) : 20000;
        long seed = args.length>2 ? Long.parseLong(args[2]) : 42L;

        Simulator sim = new Simulator(seed);
        Random rnd = new Random(seed);

        // Création des fondateurs répartis sur [-horizon, 0]
        for (int i=0;i<founders;i++) {
            double birth = -rnd.nextDouble(horizon);
            Sim founder  = new Sim(null, null, birth, Sim.Sex.getSex());  // ← OK
            sim.Birth(founder);
        }

        // Avance la simulation jusqu'à 0
        while (sim.hasEvents()) {
            Simulator.Event e = sim.getEvent();
            sim.setTime(e.getTime());
            switch (e.getEvent()) {
                case Birth -> sim.Birth(e.getSim());
                case Death -> sim.Death(e.getSim());
                case Reproduction -> sim.Reproduction(e.getSim());
                case EntersMatingAge -> sim.EntersMatingAge(e.getSim());
                case ExitsMatingAge -> sim.ExitsMatingAge(e.getSim());
            }
        }

        // Analyse
        List<Point> pat = paternal(sim, horizon);
        List<Point> mat = maternal(sim, horizon);

        System.out.println("time,paternal,maternal");
        for (int i=0; i<Math.min(pat.size(), mat.size()); i++) {
            System.out.printf(Locale.US, "%.1f,%d,%d%n", pat.get(i).time(), pat.get(i).lineages(), mat.get(i).lineages());
        }
    }
}