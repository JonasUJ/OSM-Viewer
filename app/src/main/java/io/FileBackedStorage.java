package io;

import collections.Cache;
import collections.Entity;
import collections.btree.Storage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import util.Quick;

public class FileBackedStorage<E extends Entity & Serializable> implements Storage<E> {
    private static final int MAX_SIZE = 256;

    private E lowest;
    private final FileCacheRefTable<E> table;

    private FileBackedStorage(FileCacheRefTable<E> table) {
        this.table = table;
    }

    public FileBackedStorage(Cache<FileCacheRefTable<E>, List<E>> cache) {
        try {
            this.table = new FileCacheRefTable<>(cache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFull() {
        return table.size() >= MAX_SIZE;
    }

    @Override
    public Storage<E> split(E e) {
        var other = new FileBackedStorage<>(table.cache);
        var mid = table.size() / 2;
        var median = Quick.select(table.values(), mid);

        // Copy everything after median to other
        for (int i = mid; i < table.size(); i++) {
            other.table.put(table.values().get(i));
        }

        // Clear everything just copied from this
        table.values().subList(mid, table.size()).clear();

        // Find lowest manually
        other.lowest = Collections.min(other.table.values());

        // Insert in correct half (I honestly don't care about biasing this)
        int cmp = e.compareTo(median);
        if (cmp < 0) insert(e);
        else other.insert(e);

        return other;
    }

    @Override
    public void insert(E e) {
        if (lowest == null || lowest.compareTo(e) > 0) lowest = e;
        table.put(e);
    }

    @Override
    public E lowest() {
        return lowest;
    }

    @Override
    public E get(E e) {
        return table.get(e.id());
    }

    @Override
    public boolean contains(E e) {
        if (table.isSorted()) {
            return Storage.super.contains(e);
        }

        // This implementation technically breaks the contract of this method, but:
        // All osm nodes are unique, so we can't possibly contain something before it's been added at
        // least once, and this method just so happens to only get called to check whether the element
        // is already there, before adding it.
        return false;
    }
}
