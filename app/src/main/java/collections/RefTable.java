package collections;

import java.util.*;

public class RefTable<E extends Entity> implements Iterable<E> {
    protected List<E> values = new ArrayList<>();
    protected boolean isSorted;

    public RefTable() {}

    public void put(E value) {
        isSorted = false;
        values.add(value);
    }

    public E get(long key) {
        if (!isSorted) {
            Collections.sort(values);
            isSorted = true;
        }

        var search = Collections.binarySearch(values, Entity.withId(key));

        if (search < 0) return null;
        return values.get(search);
    }

    public int size() {
        return values.size();
    }

    public boolean isSorted() {
        return isSorted;
    }

    public List<E> values() {
        return values;
    }

    @Override
    public Iterator<E> iterator() {
        return values.iterator();
    }
}
