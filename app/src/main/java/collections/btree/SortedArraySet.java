package collections.btree;

import java.util.Arrays;

class SortedArraySet<E> {
    @SuppressWarnings("unchecked")
    private final E[] inner = (E[]) new Object[BTreeSet.M];

    private int length;

    public int size() {
        return length;
    }

    public int capacity() {
        return inner.length;
    }

    public boolean isFull() {
        return size() == capacity();
    }

    public E get(int idx) {
        return inner[idx];
    }

    public SortedArraySet<E> split(int bias) {
        var other = new SortedArraySet<E>();
        final var mid = BTreeSet.HalfM; // We only split when this.isFull(), meaning the middle is static.

        // A bias less than mid will result in 8 being split 3-5 instead of 4-4 and vice versa.
        int offset = 0;
        if (bias + 1 < mid) offset = 1;
        else if (bias + 1 > mid) offset = -1;

        // Move half to other
        other.length = mid + offset;
        length -= other.length;
        for (int i = mid - offset, j = 0; j < other.size(); i++, j++) {
            other.inner[j] = inner[i];
            inner[i] = null;
        }

        return other;
    }

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
}
