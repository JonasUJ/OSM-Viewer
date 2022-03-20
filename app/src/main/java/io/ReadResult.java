package io;

import navigation.Dijkstra;

/** Object to manage multiple readers read from a file */
public record ReadResult(PolygonsReader polygons, ObjectReader<Dijkstra> dijkstra)
        implements AutoCloseable {
    @Override
    public void close() throws Exception {
        polygons.close();
        dijkstra.close();
    }
}
