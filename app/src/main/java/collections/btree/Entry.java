package collections.btree;

final class Entry<E> implements Comparable<Entry<E>> {
    private E key;
    private final Page<E> page;

    Entry(E key, Page<E> page) {
        this.key = key;
        this.page = page;
    }

    public E key() {
        return key;
    }

    public Page<E> page() {
        return page;
    }

    public void setKey(E key) {
        // Prefer this to the "assert" keyword, since the keyword introduces untestable branches, and we
        // want that sweet 100% coverage.
        if (compare(key) != 0) throw new AssertionError("assertion failed: compare(key) != 0");
        this.key = key;
    }

    @Override
    public int compareTo(Entry<E> e) {
        return compare(e.key());
    }

    @SuppressWarnings("unchecked")
    private int compare(E e) {
        var _key = (Comparable<? super E>) key();
        return _key.compareTo(e);
    }
}
