package collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RefTable<E extends Entity> implements Iterable<E> {
    List<E> values = new ArrayList<>();
    boolean isSorted;

    public RefTable() {
    }

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

    @Override
    public Iterator<E> iterator() {
        return values.iterator();
    }
}
