package pedigree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CoalescenceTest {

    @Test
    void smallPopulationCoalescesMonotonically() {
        final int FOUNDERS = 20;
        final double HORIZON = 150;         // 150 ans suffisent pour un test rapide
        Simulator sim = new Simulator(123L);

        // 20 fondateurs à t = 0
        for (int i = 0; i < FOUNDERS; i++)
            sim.Birth(new Sim(Sim.Sex.getSex()));

        // avance jusqu'à HORIZON
        while (sim.hasEvents() && sim.getTime() <= HORIZON) {
            Simulator.Event e = sim.getEvent();
            sim.setTime(e.getTime());
            switch (e.getEvent()) {
                case Birth         -> sim.Birth(e.getSim());
                case Death         -> sim.Death(e.getSim());
                case Reproduction  -> sim.Reproduction(e.getSim());
                case EntersMatingAge -> sim.EntersMatingAge(e.getSim());
                case ExitsMatingAge  -> sim.ExitsMatingAge(e.getSim());
            }
        }

        List<Coalescence.Point> pat = Coalescence.paternal(sim, HORIZON);
        List<Coalescence.Point> mat = Coalescence.maternal(sim, HORIZON);

        assertFalse(pat.isEmpty(), "liste paternelle vide");
        assertFalse(mat.isEmpty(), "liste maternelle vide");

        // séries décroissantes
        for (int i = 1; i < pat.size(); i++)
            assertTrue(pat.get(i).lineages() <= pat.get(i - 1).lineages());
        for (int i = 1; i < mat.size(); i++)
            assertTrue(mat.get(i).lineages() <= mat.get(i - 1).lineages());

        // dernière valeur ≥ 1
        assertTrue(pat.get(pat.size() - 1).lineages() >= 1);
        assertTrue(mat.get(mat.size() - 1).lineages() >= 1);
    }
}