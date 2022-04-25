package collections.btree;

class Leaf<E> implements Page<E> {
    private final Storage<E> entities;
    // Leaf<E> left;
    // Leaf<E> right;

    public Leaf(Storage<E> entities) {
        this.entities = entities;
    }

    @Override
    public Entry<E> add(E e) {
        if (entities.isFull() && !entities.contains(e)) {
            return split(e);
        }

        entities.insert(e);
        return null;
    }

    private Entry<E> split(E e) {
        var newRight = new Leaf<>(entities.split(e));

        // Insert new leaf in linked list (if we want to implement Iterator)
        // newRight.right = right;
        // newRight.left = this;
        // if (right != null)
        //     right.left = newRight;
        // right = newRight;

        return new Entry<>(newRight.entities.lowest(), newRight);
    }

    @Override
    public E get(E key) {
        return entities.get(key);
    }
}
