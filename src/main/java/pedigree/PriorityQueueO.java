package pedigree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * PriorityQueueO : tas binaire générique (min‑heap par défaut).
 * <ul>
 *   <li>insertion / poll : O(log n)</li>
 *   <li>construction par <code>addAll</code> : O(n)</li>
 * </ul>
 * @param <T> type des éléments
 */
public class PriorityQueueO<T> {

    private final ArrayList<T> heap = new ArrayList<>();
    private final Comparator<? super T> comp;

    /* ---------- Constructeurs ---------- */

    /** Ordre naturel (T doit implémenter {@link Comparable}). */
    public PriorityQueueO() {
        this(null);
    }

    /** Ordre défini par un {@link Comparator}. */
    @SuppressWarnings("unchecked")
    public PriorityQueueO(Comparator<? super T> comparator) {
        // Fallback : Comparable.cast + compareTo
        if (comparator == null) {
            this.comp = (a, b) -> ((Comparable<? super T>) a).compareTo(b);
        } else {
            this.comp = comparator;
        }
    }

    /* ---------- API publique ---------- */

    /** Ajout unique — coût <em>logarithmique</em>. */
    public void add(T element) {
        Objects.requireNonNull(element, "element must not be null");
        heap.add(element);
        siftUp(heap.size() - 1);
    }

    /**
     * Ajout en bloc de tous les éléments de {@code coll}. Utilise un <em>heapify</em>
     * bottom‑up pour restaurer la propriété du tas en O(n+m).
     */
    public void addAll(Collection<? extends T> coll) {
        Objects.requireNonNull(coll, "collection must not be null");
        for (T e : coll) {
            Objects.requireNonNull(e, "element must not be null");
            heap.add(e);
        }
        // Heapify : dernier parent = (size-2)/2
        for (int i = (heap.size() - 2) >>> 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    /** Retire et renvoie l'élément prioritaire — O(log n). */
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

    /** Consulte sans retirer. */
    public T peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /** Supprime la première occurrence de {@code element}. */
    public boolean remove(T element) {
        Objects.requireNonNull(element, "element must not be null");
        int idx = heap.indexOf(element);
        if (idx == -1) return false;

        int lastIdx = heap.size() - 1;
        if (idx == lastIdx) {
            heap.remove(lastIdx);
            return true;
        }
        T last = heap.remove(lastIdx);
        heap.set(idx, last);
        siftDown(idx);
        siftUp(idx);
        return true;
    }

    public int size()            { return heap.size(); }
    public boolean isEmpty()     { return heap.isEmpty(); }

    /* ---------- Implémentation interne ---------- */

    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) >>> 1;
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
