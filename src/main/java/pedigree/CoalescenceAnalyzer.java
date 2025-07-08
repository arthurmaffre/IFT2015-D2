package pedigree;

import java.util.*;

/**
 * Utility class for computing coalescence points for paternal and maternal lineages.
 */
public class CoalescenceAnalyzer {

    public static record Point(double time, int n){}

    /**
     * Compute coalescence series for fathers.
     * @param males living males at present
     * @return list of (time, lineages) pairs in chronological order
     */
    public static List<Point> paternal(Collection<Sim> males){
        return compute(males, Sim::getFather);
    }

    /**
     * Compute coalescence series for mothers.
     * @param females living females at present
     * @return list of (time, lineages) pairs in chronological order
     */
    public static List<Point> maternal(Collection<Sim> females){
        return compute(females, Sim::getMother);
    }

    private static List<Point> compute(Collection<Sim> start, java.util.function.Function<Sim,Sim> parent){
        // priority queue ordered by birth time descending (youngest first)
        PriorityQueue<Sim> q = new PriorityQueue<>(Comparator.comparingDouble(Sim::getBirthTime).reversed());
        Set<Sim> set = new HashSet<>();
        for (Sim s : start){
            q.add(s);
            set.add(s);
        }
        List<Point> result = new ArrayList<>();
        double now = start.stream().mapToDouble(Sim::getBirthTime).max().orElse(0.0);
        result.add(new Point(now, set.size()));
        while (set.size() > 1 && !q.isEmpty()){
            Sim s = q.poll();
            set.remove(s);
            Sim p = parent.apply(s);
            if (p != null){
                if (set.contains(p)){
                    result.add(new Point(s.getBirthTime(), set.size()));
                }else{
                    set.add(p);
                    q.add(p);
                }
            }else{
                // founder with no parent
                result.add(new Point(s.getBirthTime(), set.size()));
            }
        }
        return result;
    }
}
