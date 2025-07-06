package pedigree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

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

        // Rétablit les propriétés du tas :
        // on tente les deux opérations (l'une sera neutre)
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