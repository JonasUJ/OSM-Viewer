package collections.btree;

public interface Storage<E> {
    boolean isFull();

    Storage<E> split(E e);

    void insert(E e);

    E lowest();

    E get(E e);

    default boolean contains(E e) {
        return get(e) != null;
    }
}
