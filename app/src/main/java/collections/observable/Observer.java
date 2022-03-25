package collections.observable;

@FunctionalInterface
public interface Observer<T> {
    void onChange(T event);
}
