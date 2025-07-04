package pedigree;



public class simulate {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java simulate <population_size> <tMax>");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);
        int tMax = Integer.parseInt(args[1]);

        Simulator simulator = new Simulator();

        System.out.println("=== Simulation Start ===");
        System.out.println("Initial population: " + n);
        System.out.println("Running until time: " + tMax);
        System.out.println("-------------------------");

        // Créer la population initiale (founders)
        for (int i = 0; i < n; i++) {
            Sim.Sex sex = Sim.Sex.getSex();
            Sim founder = new Sim(sex);
            simulator.Birth(founder);
        }

        int eventsProcessed = 0;

        // Boucle principale de la simulation
        while (simulator.hasEvents()) {
            if (simulator.getTime() > tMax) break;

            Simulator.Event E = simulator.getEvent();
            simulator.setTime(E.getTime());

            if (E.getTime() < E.getSim().getDeathTime()) {
                switch (E.getEvent()) {
                    case Birth:
                        simulator.Birth(E.getSim());
                        break;
                    case Death:
                        simulator.Death(E.getSim());
                        break;
                    case Reproduction:
                        simulator.Reproduction(E.getSim());
                        break;
                }
            }
            eventsProcessed++;
        }

        System.out.println("-------------------------");
        System.out.println("Simulation complete.");
        System.out.println("Final time: " + simulator.getTime());
        System.out.println("Events processed: " + eventsProcessed);
    }
}