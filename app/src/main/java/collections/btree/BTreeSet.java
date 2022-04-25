package collections.btree;

import java.util.Objects;

/**
 * B+ Tree, but only the features we need.
 *
 * <p>See Algorithms 4th ed. chapter 6 (p. 866) for an introduction to B-trees
 *
 * @param <E> Type of objects held in the tree. Must implement Comparable&lt;E&gt;. If E does not, a
 *     ClassCastException will be thrown at an unspecified time.
 */
@SuppressWarnings("rawtypes")
public class BTreeSet<E> {
    static final int M = 16;
    static final int HalfM = M / 2;
    private static final Object sentinel = (Comparable) e -> -1;

    static {
        if ((M & 1) == 1) throw new AssertionError("BTree.M must be even");
    }

    Page<E> root;

    public BTreeSet() {
        root = new Leaf<>(new SortedArraySet<>());
    }

    /**
     * Add element e to the set, overwriting it if an equal element already exists.
     *
     * @param e Element to add.
     * @throws NullPointerException If e is null.
     */
    @SuppressWarnings("unchecked")
    public void add(E e) {
        Objects.requireNonNull(e);

        var split = root.add(e);
        if (split == null) return;

        // height++;
        var newRoot = new Internal<E>();

        // Root is not Internal for the first M add calls
        if (root instanceof Internal<E> internal) {
            var entry = internal.entries.lowest();
            newRoot.entries.insert(new Entry<>(entry.key(), root));
        } else {
            newRoot.entries.insert(new Entry<>((E) sentinel, root));
        }
        newRoot.entries.insert(split);

        root = newRoot;
    }

    /**
     * Get the object that compares equal to the passed key, or null if no such object exists.
     *
     * @param key The key to search for.
     * @throws NullPointerException If key is null.
     * @return An object that compares equal to the passed key, or null if no such object exists.
     */
    public E get(E key) {
        Objects.requireNonNull(key);

        return root.get(key);
    }

    /**
     * Test whether an object that compares equal to the passed key exists in the set.
     *
     * @param key The key to search for.
     * @throws NullPointerException If key is null.
     * @return Whether an object that compares equal to the passed key exists in the set.
     */
    public boolean contains(E key) {
        return get(key) != null;
    }
}
