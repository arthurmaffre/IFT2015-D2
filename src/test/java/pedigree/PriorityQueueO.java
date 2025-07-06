package pedigree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PriorityQueueO : tas binaire min-heap (log n insert, poll et remove).
 *
 * @param <T> type des éléments
 */
public class PriorityQueueO<T> {

    private final ArrayList<T> heap = new ArrayList<>();
    private final Comparator<? super T> comp;

    /* ---------- Constructeurs ---------- */

    /** Ordre naturel (T doit implémenter Comparable). */
    public PriorityQueueO() {
        this(null);
    }

    /** Ordre défini par un Comparator. */
    public PriorityQueueO(Comparator<? super T> comparator) {
        // Si aucun comparator fourni, on en crée un qui s'appuie sur Comparable
        if (comparator == null) {
            this.comp = (a, b) -> ((Comparable<? super T>) a).compareTo(b);
        } else {
            this.comp = comparator;
        }
    }

    /* ---------- API publique ---------- */

    /** Ajout O(log n). */
    public void add(T element) {
        Objects.requireNonNull(element, "element must not be null");
        heap.add(element);
        siftUp(heap.size() - 1);
    }

    /**
     * Retire et renvoie l'élément prioritaire (le plus petit).
     * O(log n).
     */
    public T poll() {
        if (heap.isEmpty()) return null;
        T root = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    /** Renvoie sans retirer l'élément prioritaire. */
    public T peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /**
     * Supprime la première occurrence de {@code element} (égalité via {@code equals}).
     * @return {@code true} si l'élément était présent et a été retiré.
     *         Complexité : O(n) pour la recherche + O(log n) pour rétablir le tas.
     */
    public boolean remove(T element) {
        Objects.requireNonNull(element, "element must not be null");
        int idx = heap.indexOf(element);           // recherche linéaire
        if (idx == -1) return false;               // non présent

        int lastIdx = heap.size() - 1;
        if (idx == lastIdx) {                      // dernier élément
            heap.remove(lastIdx);
            return true;
        }

        T last = heap.remove(lastIdx);
        heap.set(idx, last);

        // Rétablit les propriétés du tas
        siftDown(idx);
        siftUp(idx);
        return true;
    }

    public int size()    { return heap.size(); }
    public boolean isEmpty() { return heap.isEmpty(); }

    /* ---------- Helpers internes ---------- */

    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) >>> 1;                   // division entière par 2
            if (comp.compare(heap.get(idx), heap.get(parent)) >= 0) break;
            swap(idx, parent);
            idx = parent;
        }
    }

    private void siftDown(int idx) {
        int n = heap.size();
        while (true) {
            int left  = (idx << 1) + 1;
            int right = left + 1;
            int smallest = idx;

            if (left  < n && comp.compare(heap.get(left), heap.get(smallest))  < 0) smallest = left;
            if (right < n && comp.compare(heap.get(right), heap.get(smallest)) < 0) smallest = right;

            if (smallest == idx) break;
            swap(idx, smallest);
            idx = smallest;
        }
    }

    private void swap(int i, int j) {
        T tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }
}

/* ======================================================================= */
/* ======================= JUnit 5 Test Suite ============================ */
/* ======================================================================= */

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

    /* ---------- Nouveaux tests : remove() ---------- */

    @Test
    void removeExistingElement() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(3); pq.add(1); pq.add(2);

        assertTrue(pq.remove(2));            // élément au milieu
        assertEquals(1, pq.poll());
        assertEquals(3, pq.poll());
        assertTrue(pq.isEmpty());
    }

    @Test
    void removeHeadElement() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(2); pq.add(5); pq.add(7);

        assertTrue(pq.remove(2));            // le head
        assertEquals(5, pq.peek());
        assertEquals(5, pq.poll());
        assertEquals(7, pq.poll());
    }

    @Test
    void removeNotPresent() {
        PriorityQueueO<Integer> pq = new PriorityQueueO<>();
        pq.add(1); pq.add(2);

        assertFalse(pq.remove(3));           // absent
        assertEquals(1, pq.poll());
        assertEquals(2, pq.poll());
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