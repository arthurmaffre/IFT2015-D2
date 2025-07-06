import pedigree.Sim;
import pedigree.Simulator;

class simulate {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int tMax = Integer.parseInt(args[1]);
        // population et temps max
        Simulator simulator = new Simulator(); // file de priorité
        for (int i=0; i<n; i++){
            Sim.Sex sex = Sim.Sex.getSex();
            Sim founder = new Sim(sex);
            simulator.Birth(founder);
        }
        while (simulator.hasEvents()){
            if (tMax < simulator.getTime()) break; // arrêter à tMax
            Simulator.Event E = simulator.getEvent();
            if (E.getTime() < simulator.getTime()) {
                throw new RuntimeException("Time loop");
            }
            simulator.setTime(E.getTime());

            switch (E.getEvent()) {
                case Birth:
                    simulator.Birth(E.getSim());
                    break;
                case Death:
                    simulator.Death(E.getSim());
                    break;
                case EntersMatingAge:
                    simulator.EntersMatingAge(E.getSim());
                    break;
                case ExitsMatingAge:
                    simulator.ExitsMatingAge(E.getSim());
                    break;
                case Reproduction:
                    simulator.Reproduction(E.getSim());
                    break;
            }
            System.out.println(simulator.getTime() + ":" + simulator.getPopulation());
        }
        }
}
