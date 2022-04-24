package collections.btree;

class Leaf<E> implements Page<E> {
    private final SortedArraySet<E> entities;
    // Leaf<E> left;
    // Leaf<E> right;

    public Leaf() {
        entities = new SortedArraySet<>();
    }

    private Leaf(SortedArraySet<E> entities) {
        this.entities = entities;
    }

    @Override
    public Entry<E> add(E e) {
        if (entities.isFull()) {
            var idx = entities.find(e);
            return split(e, idx);
        }

        entities.insert(e);
        return null;
    }

    private Entry<E> split(E e, int idx) {
        var newRight = new Leaf<>(entities.split(idx));

        // Insert e in correct leaf
        if (idx <= entities.capacity() >> 1) entities.insert(e);
        else newRight.entities.insert(e);

        // Insert new leaf in linked list (if we want to implement Iterator)
        // newRight.right = right;
        // newRight.left = this;
        // if (right != null)
        //     right.left = newRight;
        // right = newRight;

        return new Entry<>(newRight.entities.get(0), newRight);
    }

    @Override
    public E get(E key) {
        var idx = -entities.find(key) - 1;
        if (idx < 0) return null;

        return entities.get(idx);
    }
}
