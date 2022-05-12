package collections.lru;

import collections.Cache;
import java.util.*;

public class LRUCache<K, V> implements Cache<K, V> {
    private int capacity;
    private final Map<K, Node<K, V>> nodeMap;
    private final NodeList<K, V> evictionQueue;

    public LRUCache() {
        this(256);
    }

    public LRUCache(int capacity) {
        nodeMap = new HashMap<>();
        evictionQueue = new NodeList<>();
        setCapacity(capacity);
    }

    @Override
    public int size() {
        return evictionQueue.size();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be greater than 0");
        this.capacity = capacity;
        ensureSize(capacity);
    }

    @Override
    public void clear() {
        nodeMap.clear();
        ensureSize(0);
    }

    @Override
    public void set(K key, V value) {
        var node = nodeMap.get(key);

        // Update existing
        if (node != null) {
            node.set(value);
            evictionQueue.touch(node);
            return;
        }

        // Add new
        node = new Node<>(key, value);
        nodeMap.put(key, node);
        evictionQueue.add(node);

        ensureSize(capacity);
    }

    @Override
    public V get(K key) {
        var node = nodeMap.get(key);
        if (node == null) return null;

        evictionQueue.touch(node);
        return node.get();
    }

    private void ensureSize(int size) {
        while (evictionQueue.size() > size) {
            var evicted = evictionQueue.pop();
            nodeMap.remove(evicted.key);

            if (evicted.get() instanceof EvictionAware aware) {
                aware.onEvicted();
            }
        }
    }
}
