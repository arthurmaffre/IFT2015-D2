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
            simulator.setTime(E.getTime()); // prochain événement par E.time
            if (E.getTime() < E.getSim().getDeathTime()){
                switch (E.getEvent()) {
                    case Birth: simulator.Birth(E.getSim());
                    case Death: simulator.Death(E.getSim());
                    case Reproduction: simulator.Reproduction(E.getSim());
                }
            }
        }
        }
}
