package collections;

public interface Cache<K, V> {
    int size();

    int getCapacity();

    void setCapacity(int capacity);

    void clear();

    void set(K key, V value);

    V get(K key);
}
