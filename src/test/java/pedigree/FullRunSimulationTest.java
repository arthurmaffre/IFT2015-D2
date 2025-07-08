package pedigree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that runs the simulator for a short period
 * of time and checks that events are processed in chronological
 * order and that the population never becomes negative.
 */
@Timeout(10)
public class FullRunSimulationTest {

    /**
     * Executes the event loop until either no events remain or the
     * horizon is reached. After each step we ensure that events are
     * processed in chronological order and the population count is
     * non‑negative.
     */
    @Test
    @DisplayName("Exécution complète de la simulation sur une courte période")
    void testEventLoopRunsSmoothly() {
        Simulator sim = new Simulator();

        // --- Population fondatrice ---
        final int FOUNDERS = 1000;
        for (int i = 0; i < FOUNDERS; i++) {
            sim.Birth(new Sim(Sim.Sex.getSex()));
        }

        double lastTime = 0.0;
        final double HORIZON = 200.0;

        System.out.println("time,population");
        while (sim.hasEvents()) {
            Simulator.Event ev = sim.getEvent();
            assertNotNull(ev, "Les événements doivent être non nuls");
            assertTrue(ev.getTime() >= lastTime,
                       "Les événements doivent être ordonnés chronologiquement");
            lastTime = ev.getTime();
            if (lastTime > HORIZON) break; // limite de temps

            sim.setTime(ev.getTime());
            switch (ev.getEvent()) {
                case Birth -> sim.Birth(ev.getSim());
                case Death -> sim.Death(ev.getSim());
                case Reproduction -> sim.Reproduction(ev.getSim());
                case EntersMatingAge -> sim.EntersMatingAge(ev.getSim());
                case ExitsMatingAge -> sim.ExitsMatingAge(ev.getSim());
            }

            // Affiche l'état courant de la simulation
            System.out.printf("%.1f,%d%n", lastTime, sim.getPopulation());

            assertTrue(sim.getPopulation() >= 0, "Population toujours positive");
        }

        System.out.println("Population finale: " + sim.getPopulation());
    }
}
