package collections.lru;

@FunctionalInterface
public interface EvictionAware {
    void onEvicted();
}
