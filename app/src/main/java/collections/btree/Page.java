package collections.btree;

interface Page<E> {
    Entry<E> add(E key);

    E get(E key);
}
