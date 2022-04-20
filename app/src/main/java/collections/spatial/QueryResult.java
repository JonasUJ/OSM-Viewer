package collections.spatial;

import geometry.Point;

public record QueryResult<E>(Point point, E value) {}
