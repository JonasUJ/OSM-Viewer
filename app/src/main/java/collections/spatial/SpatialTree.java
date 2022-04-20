package collections.spatial;

import geometry.Point;
import geometry.Rect;

public interface SpatialTree<E> {
    int size();

    void insert(Point point, E value);

    boolean contains(Point point);

    QueryResult<E> nearest(Point query);

    Iterable<QueryResult<E>> range(Rect query);

    default boolean isEmpty() {
        return size() == 0;
    }
}
