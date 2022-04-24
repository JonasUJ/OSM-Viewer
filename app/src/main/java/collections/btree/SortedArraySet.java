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
        var mid = size() >> 1;

        // A bias less than mid will result in 8 being split 3-5 instead of 4-4 and vice versa.
        int offset = 0;
        if (bias < mid) offset = 1;
        else if (bias > mid) offset = -1;

        // Copy half to new set
        other.length = mid + offset;
        System.arraycopy(inner, mid - offset, other.inner, 0, other.size());

        // Remove copied half from this
        length -= other.length;
        Arrays.fill(inner, size(), capacity(), null);

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
