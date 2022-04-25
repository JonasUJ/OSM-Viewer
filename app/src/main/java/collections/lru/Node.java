package collections.lru;

class Node<K, V> {
    public final K key;
    private V value;
    Node<K, V> left;
    Node<K, V> right;

    public Node(K key, V value) {
        this.key = key;
        set(value);
    }

    public V get() {
        return value;
    }

    public void set(V value) {
        this.value = value;
    }
}
