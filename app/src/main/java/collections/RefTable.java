package collections;

import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import sort.QuickSort;

public class RefTable {
    LongList keys = new LongList();
    IntList values = new IntList();
    boolean isSorted;

    public RefTable() {}

    public void put(long key, int value) {
        isSorted = false;
        keys.add(key);
        values.add(value);
    }

    public int get(long key) {
        if (!isSorted) sortByKeys(Long::compare);

        var search = keys.search(key);

        if (search < 0) return -1;
        return values.get(search);
    }

    public int size() {
        return keys.size();
    }

    public LongList keys() {
        return keys;
    }

    public IntList values() {
        return values;
    }

    public void sortByKeys(LongBinaryOperator cmp) {
        QuickSort.sort(
                keys.array,
                0,
                keys.size(),
                cmp,
                (i, j) -> {
                    keys.swap((int) i, (int) j);
                    values.swap((int) i, (int) j);
                });
        isSorted = true;
    }

    public void sortByValues(IntBinaryOperator cmp) {
        QuickSort.sort(
                values.array,
                0,
                values.size(),
                cmp,
                (i, j) -> {
                    keys.swap(i, j);
                    values.swap(i, j);
                });
        isSorted = false;
    }
}
