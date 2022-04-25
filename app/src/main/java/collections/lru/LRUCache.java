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
        cleanup();
    }

    public void clear() {
        nodeMap.clear();
        evictionQueue.clear();
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

        cleanup();
    }

    public V get(K key) {
        var node = nodeMap.get(key);
        if (node == null) return null;

        evictionQueue.touch(node);
        return node.get();
    }

    private void cleanup() {
        while (evictionQueue.size() > getCapacity()) {
            var evicted = evictionQueue.pop();
            nodeMap.remove(evicted.key);

            if (evicted.get() instanceof EvictionAware aware) {
                aware.onEvicted();
            }
        }
    }
}
