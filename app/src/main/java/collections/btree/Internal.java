package collections.btree;

class Internal<E> implements Page<E> {
    final SortedArraySet<Entry<E>> entries;

    public Internal() {
        entries = new SortedArraySet<>();
    }

    private Internal(SortedArraySet<Entry<E>> entries) {
        this.entries = entries;
    }

    @Override
    public Entry<E> add(E e) {
        var idx = entries.find(new Entry<>(e, null)) - 1;

        if (idx < 0) {
            idx = -idx - 2;
            entries.get(idx).setKey(e); // Prevent loitering
        }

        var entry = entries.get(idx);
        var split = entry.page().add(e);
        if (split == null) return null;

        if (entries.isFull()) {
            return split(split, idx);
        }

        entries.insert(split);
        return null;
    }

    private Entry<E> split(Entry<E> e, int idx) {
        var newPage = new Internal<>(entries.split(idx));

        // Insert e in correct page
        if (idx <= BTreeSet.HalfM) entries.insert(e);
        else newPage.entries.insert(e);

        var entry = newPage.entries.get(0);
        return new Entry<>(entry.key(), newPage);
    }

    @Override
    public E get(E key) {
        var idx = entries.find(new Entry<>(key, null)) - 1;
        if (idx < 0) idx = -idx - 2;

        // Recursive call if page() instanceof Internal
        return entries.get(idx).page().get(key);
    }
}
