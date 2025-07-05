package pedigree;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PriorityQueueOTest {

    /* ---------- Cas simples ---------- */

    @Test
    void addPollNaturalOrder() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(10);
        pq.add(5);
        pq.add(7);

        assertEquals(5, pq.poll());
        assertEquals(7, pq.poll());
        assertEquals(10, pq.poll());
        assertTrue(pq.isEmpty());
    }

    @Test
    void addPollCustomComparatorMaxHeap() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>(Comparator.reverseOrder());
        pq.add(1);
        pq.add(3);
        pq.add(2);

        assertEquals(3, pq.poll());
        assertEquals(2, pq.poll());
        assertEquals(1, pq.poll());
    }

    @Test
    void peekDoesNotRemove() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(20);
        pq.add(15);

        assertEquals(15, pq.peek());
        assertEquals(15, pq.peek());
        assertEquals(15, pq.poll());
        assertEquals(20, pq.peek());
        assertEquals(1, pq.size());
    }

    @Test
    void handlesDuplicates() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(4);
        pq.add(4);
        pq.add(2);

        assertEquals(2, pq.poll());
        assertEquals(4, pq.poll());
        assertEquals(4, pq.poll());
    }

    @Test
    void nullInsertionThrows() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        assertThrows(NullPointerException.class, () -> pq.add(null));
    }

    @Test
    void pollAndPeekOnEmpty() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        assertNull(pq.peek());
        assertNull(pq.poll());
    }

    /* ---------- Test de propriété (robustesse) ---------- */

    /**  
     * Insère N nombres aléatoires, puis vérifie que les sorties sont triées.
     * Répété 5 fois pour maximiser la couverture.
     */
    @RepeatedTest(5)
    void randomPropertyTest() {
        final int N = 10_000;
        Random rnd = new Random();

        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        List<Integer> reference = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            int value = rnd.nextInt();
            pq.add(value);
            reference.add(value);
        }
        // Vérifie la taille
        assertEquals(N, pq.size());

        // Tri de référence
        reference.sort(Integer::compareTo);

        // Collecte des sorties du tas
        List<Integer> output =
                IntStream.range(0, N)
                         .mapToObj(i -> pq.poll())
                         .collect(Collectors.toList());

        assertEquals(reference, output);          // ordre total préservé
        assertTrue(pq.isEmpty());
    }
}