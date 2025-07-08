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
 * (chromosome Y) et maternelles (ADN mt) représentées dans la population
 * vivante à un instant donné.
 * <p>Algorithme § 2.3 : tas « plus jeune d’abord » + Set pour tester la fusion.</p>
 */
public final class Coalescence {

    /** Point (t, n) : date (année < 0 passé, 0 présent) et nombre de lignées. */
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
                                       double horizon) {
        // Tas trié « plus jeune d’abord » (birthTime décroissant)
        PriorityQueueO<Sim> pq = new PriorityQueueO<>((a, b) -> Double.compare(b.getBirthTime(), a.getBirthTime()));
        pq.addAll(pop);

        HashSet<Sim> active = new HashSet<>(pop);
        List<Point> traj = new ArrayList<>();
        int n = active.size();
        // 0 = temps présent
        traj.add(new Point(0.0, n));

        while (n > 1 && !pq.isEmpty()) {
            Sim child = pq.poll();
            Sim parent = parentSel.apply(child);
            if (parent == null) continue;                   // fondateur

            if (!active.add(parent)) {                      // déjà présent → fusion
                n--;
                traj.add(new Point(horizon - child.getBirthTime(), n));
            } else {
                pq.add(parent);                            // nouvelle lignée à explorer
            }

        }
        return traj;
    }

    /* =================== Exécutable CLI =================== */

    public static void main(String[] args) {
        int founders = args.length > 0 ? Integer.parseInt(args[0]) : 1000;
        double horizon = args.length > 1 ? Double.parseDouble(args[1]) : 20000;
        long seed = args.length > 2 ? Long.parseLong(args[2]) : 42L;

        Simulator sim = new Simulator(seed, horizon);
        Random rnd = new Random(seed);

        /* ----------- Création des fondateurs & planification Birth ----------- */
        for (int i = 0; i < founders; i++) {
            Sim founder = new Sim(Sim.Sex.getSex());
            sim.scheduleBirthEvent(founder);
        }

        /* ------------------- Boucle d’exécution des événements ------------------- */
        while (sim.hasEvents()) {
            Simulator.Event e = sim.getEvent();
            if (e.getTime() > horizon) break;              // stop at horizon
            sim.setTime(e.getTime());
            switch (e.getEvent()) {
                case Birth          -> sim.Birth(e.getSim());
                case Death          -> sim.Death(e.getSim());
                case Reproduction   -> sim.Reproduction(e.getSim());
                case EntersMatingAge-> sim.EntersMatingAge(e.getSim());
                case ExitsMatingAge -> sim.ExitsMatingAge(e.getSim());
            }
        }
        // force un dernier échantillon à horizon
        sim.setTime(horizon);
        sim.recordSample();

        /* ------------------- Impression des résultats ------------------- */
        // (1) Population vivante tous les 100 ans
        System.out.println("time,population");
        for (Simulator.PointPop p : sim.getPopSamples()) {
            System.out.printf(Locale.US, "%.1f,%d%n", p.time(), p.pop());
        }

        // (2) Coalescence
        List<Point> pat = paternal(sim, horizon);
        List<Point> mat = maternal(sim, horizon);

        System.out.println("time,paternal,maternal");
        int m = Math.min(pat.size(), mat.size());
        for (int i = 0; i < m; i++) {
            System.out.printf(Locale.US, "%.1f,%d,%d%n", pat.get(i).time(), pat.get(i).lineages(), mat.get(i).lineages());
        }
    }
}