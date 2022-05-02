package collections;

import java.util.ArrayList;
import java.util.List;

public class ArrayListRefTable<E extends Entity> extends RefTable<E> {
    private List<E> values = new ArrayList<>();

    @Override
    public List<E> values() {
        return values;
    }
}
