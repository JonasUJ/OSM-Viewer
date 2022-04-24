package collections.btree;

import java.util.Arrays;

class SortedArraySet<E> implements Storage<E> {
    @SuppressWarnings("unchecked")
    private final E[] inner = (E[]) new Object[BTreeSet.M];

    private int length;

    public int size() {
        return length;
    }

    public int capacity() {
        return inner.length;
    }

    @Override
    public boolean isFull() {
        return size() == capacity();
    }

    public E get(int idx) {
        return inner[idx];
    }

    @Override
    public SortedArraySet<E> split(E e) {
        var bias = find(e);
        var other = new SortedArraySet<E>();
        final var mid =
                BTreeSet.HalfM; // We only split when this.isFull(), meaning the middle is static.

        // A bias less than mid will result in 8 being split 3-5 instead of 4-4 and vice versa.
        int offset = 0;
        if (bias < mid) offset = 1;
        else if (bias > mid) offset = -1;

        // Move half to other
        other.length = mid + offset;
        length -= other.length;
        for (int i = mid - offset, j = 0; j < other.size(); i++, j++) {
            other.inner[j] = inner[i];
            inner[i] = null;
        }

        // Insert e in correct set
        if (bias < mid) insert(e);
        else other.insert(e);

        return other;
    }

    @Override
    public void insert(E e) {
        var idx = find(e);

        if (idx < 0) {
            inner[-idx - 1] = e;
            return;
        }

        System.arraycopy(inner, idx, inner, idx + 1, length - idx);
        inner[idx] = e;
        length++;
    }

    public int find(E e) {
        return -Arrays.binarySearch(inner, 0, length, e, null) - 1;
    }

    @Override
    public E lowest() {
        return get(0);
    }

    @Override
    public E get(E e) {
        var idx = -find(e) - 1;
        if (idx < 0) return null;

        return get(idx);
    }

    @Override
    public boolean contains(E e) {
        return find(e) < 0;
    }
}
