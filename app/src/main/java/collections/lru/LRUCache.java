package collections.lru;

import java.util.*;

public class LRUCache<K, V> {
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

    public int size() {
        return evictionQueue.size();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be greater than 0");
        this.capacity = capacity;
        ensureSize(capacity);
    }

    public void clear() {
        nodeMap.clear();
        ensureSize(0);
    }

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
