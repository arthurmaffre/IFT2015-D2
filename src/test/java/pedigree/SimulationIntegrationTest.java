package pedigree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests d’intégration et unitaires pour la classe {@link Simulator}.
 * <p>
 * Le but est de vérifier :
 * <ul>
 *   <li>La cohérence de l’horloge interne (getTime / setTime) ;</li>
 *   <li>L’évolution raisonnable de la population (jamais négative) ;</li>
 *   <li>Le fonctionnement du moteur d’événements (hasEvents / getEvent) ;</li>
 *   <li>Le cycle de reproduction (Birth –> Reproduction –> Birth) ;</li>
 *   <li>Le passage à l’âge de reproduction et la sortie (Enters/ExitsMatingAge) ;</li>
 *   <li>La suppression correcte d’un individu à sa mort.</li>
 * </ul>
 * Chaque test est indépendant et se veut minimal pour que vous puissiez
 * facilement le faire évoluer.
 */
@Timeout(5) // coupe l’exécution au‑delà de 5 s
public class SimulationIntegrationTest {
    private Simulator sim;

    /**
     * Instancie un nouveau {@link Simulator} avant chaque test afin de
     * garantir l’indépendance des scénarios.
     */
    @BeforeEach
    void setUp() {
        sim = new Simulator();
    }

    // ---------------------------------------------------------------------
    // Horloge interne
    // ---------------------------------------------------------------------
    @Test
    @DisplayName("Horloge : getTime / setTime")
    void testClock() {
        // Temps initial == 0, par contrat de la classe.
        assertEquals(0.0, sim.getTime(), 1e-9, "Le temps initial doit être 0");

        // Avance arbitrairement l’horloge et vérifie la mise à jour.
        double newT = 42.5;
        sim.setTime(newT);
        assertEquals(newT, sim.getTime(), 1e-9, "setTime ne met pas correctement à jour calendarTime");
    }

    // ---------------------------------------------------------------------
    // Population & événements de base
    // ---------------------------------------------------------------------
    @Test
    @DisplayName("Birth initial : la population augmente et des événements sont planifiés")
    void testFounderBirth() {
        // Crée deux fondateurs (null parents) nés au temps 0.
        Sim eve  = new Sim(null, null, 0.0, Sim.Sex.F);
        Sim adam = new Sim(null, null, 0.0, Sim.Sex.M);

        // Les naissances doivent incrémenter la population.
        sim.Birth(eve);
        assertEquals(1, sim.getPopulation(), "Population après la naissance d’Eve incorrecte");

        sim.Birth(adam);
        assertEquals(2, sim.getPopulation(), "Population après la naissance d’Adam incorrecte");

        // Au moins un événement doit maintenant être présent (reproduction ou mort).
        assertTrue(sim.hasEvents(), "Aucun événement n’a été planifié après les naissances fondateur");
    }

    @Test
    @DisplayName("Attributs : le sexe des individus doit être correct")
    void testSexAttribution() {
        Sim female = new Sim(null, null, 0.0, Sim.Sex.F);
        Sim male = new Sim(null, null, 0.0, Sim.Sex.M);

        assertEquals(Sim.Sex.F, female.getSex(), "Le sexe de female devrait être F");
        assertEquals(Sim.Sex.M, male.getSex(), "Le sexe de male devrait être M");
    }

    @Test
    @DisplayName("Birth() programme automatiquement des événements")
    void testBirthSchedulesEvents() {
        Simulator sim = new Simulator();
        Sim eve = new Sim(null, null, 0.0, Sim.Sex.F);

        sim.Birth(eve);

        assertTrue(sim.hasEvents(), "La file d'événements doit contenir au moins un event après Birth()");

        Simulator.Event ev = sim.getEvent();
        assertNotNull(ev, "getEvent() ne doit pas renvoyer null");
        assertEquals(eve, ev.getSim(), "L'event doit concerner Eve");
    }


    @Test
    @DisplayName("Event : les attributs doivent être correctement assignés et accessibles")
    void testEventAttributes() {
        Sim testSim = new Sim(null, null, 0.0, Sim.Sex.F);
        Simulator.Event event = sim.new Event(Simulator.Events.Birth, testSim, 10.5);

        assertEquals(Simulator.Events.Birth, event.getEvent(), "L'événement devrait être de type Birth");
        assertEquals(testSim, event.getSim(), "Le Sim associé ne correspond pas");
        assertEquals(10.5, event.getTime(), 1e-9, "Le temps de l'événement est incorrect");
    }

    @Test
    @DisplayName("Événement : les enfants de fondateurs se reproduisent")
    void testChildrenReproduceViaEvent() {
        // --- 0. Préparation ---
        Simulator sim = new Simulator();

        // --- 1. Fondateurs ---
        Sim eve  = new Sim(null, null, 0.0, Sim.Sex.F);
        Sim adam = new Sim(null, null, 0.0, Sim.Sex.M);
        sim.Birth(eve);
        sim.Birth(adam);
        assertEquals(2, sim.getPopulation(), "Population après fondateurs");

        // --- 2. Avance rapide pour que les fondateurs puissent avoir des enfants ---
        sim.setTime(Sim.MIN_MATING_AGE_F + 0.5);

        // --- 3. Deux enfants de sexes opposés ---
        Sim childF = new Sim(eve,  adam, sim.getTime(), Sim.Sex.F);
        Sim childM = new Sim(eve,  adam, sim.getTime(), Sim.Sex.M);
        sim.Birth(childF);
        sim.Birth(childM);
        assertEquals(4, sim.getPopulation(), "Population après naissance des enfants");

        // --- 4. Avance rapide : les enfants deviennent adultes ---
        sim.setTime(sim.getTime() + Sim.MIN_MATING_AGE_F + 0.5);

        // Le fils entre officiellement sur le “marché” des partenaires.
        sim.EntersMatingAge(childM);

        // --- 5. On déclenche la reproduction via l’événement Reproduction ---
        sim.Reproduction(childF);   // simulateur choisit automatiquement childM (seul mâle dispo)

        // --- 6. Vérification ---
        assertEquals(5, sim.getPopulation(),
                "La population doit avoir augmenté de 1 après la reproduction des enfants");
    }


    @Test
    @DisplayName("Mort : suppression correcte des individus du simulateur")
    void testDeathRemovesSimFromPopulation() {
        Sim eve  = new Sim(null, null, 0.0, Sim.Sex.F);
        Sim adam = new Sim(null, null, 0.0, Sim.Sex.M);
        sim.Birth(eve);
        sim.Birth(adam);

        // Population initiale : 2
        assertEquals(2, sim.getPopulation(), "Population initiale incorrecte");

        // 1. On élimine Eve
        sim.Death(eve);
        assertEquals(1, sim.getPopulation(), "Eve n’a pas été retirée");

        // 2. On élimine Adam
        sim.Death(adam);
        assertEquals(0, sim.getPopulation(), "Adam n’a pas été retiré");
    }

    @Test
    @DisplayName("1️⃣  Reproduction simple : un enfant naît, population +1")
    void testSimpleReproduction() {
        // Avance : fondateurs prêts
        sim.setTime(Sim.MIN_MATING_AGE_F + 0.1);

        // Eve se reproduit (Adam est dispo, pas de fidélité en jeu)
        Sim eve = sim.getEvent().getSim();   // premier event = Reproduction(Eve)
        sim.Reproduction(eve);

        assertEquals(3, sim.getPopulation(), "Population doit passer de 2 à 3");
    }

    @Test
    @DisplayName("2️⃣  Pas de mâle disponible ⇒ aucune naissance")
    void testNoMaleNoBirth() {
        Sim loneFemale = new Sim(null, null, 0.0, Sim.Sex.F);
        sim.Birth(loneFemale);

        sim.setTime(Sim.MIN_MATING_AGE_F + 0.1);
        sim.Reproduction(loneFemale);

        assertEquals(1, sim.getPopulation(),
                     "Aucun mâle libre : la population reste à 1");
    }

    @Test
    @DisplayName("3️⃣  Un parent mort ne se reproduit plus")
    void testDeadParentNoReproduction() {
        Sim eve = sim.getEvent().getSim();     // Eve (F)

        // Tue Eve immédiatement
        sim.Death(eve);
        assertEquals(1, sim.getPopulation(), "Eve retirée de la population");

        // Avance quand même le temps et tente Reproduction
        sim.setTime(Sim.MIN_MATING_AGE_F + 0.5);
        sim.Reproduction(eve);

        assertEquals(1, sim.getPopulation(),
                     "Eve est morte : aucune nouvelle naissance");
    }


    @Test
    @DisplayName("4a  Reproduction planifiée jusqu’à l’âge limite")
    void testRepeatedReproductionUntilAgeLimit() {
        Sim eve = sim.getEvent().getSim();   // premier event de Reproduction(Eve)

        int births = 0;
        while (sim.hasEvents()) {
            Simulator.Event ev = sim.getEvent();
            sim.setTime(ev.getTime());

            switch (ev.getEvent()) {
                case Reproduction -> {
                    sim.Reproduction(ev.getSim());
                    births++;
                }
                case EntersMatingAge -> sim.EntersMatingAge(ev.getSim());
                case ExitsMatingAge  -> sim.ExitsMatingAge(ev.getSim());
                case Death           -> sim.Death(ev.getSim());
                default -> {}
            }
        }
        // Eve devrait enfanter plusieurs fois mais s’arrêter avant son décès
        assertTrue(births > 0, "Au moins un enfant est né");
        assertEquals(sim.getPopulation(), 2 + births,
                        "Population = fondateurs + naissances");
    }

}