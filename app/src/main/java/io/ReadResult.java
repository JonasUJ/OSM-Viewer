package io;

import Search.AddressDatabase;
import navigation.Dijkstra;
import osm.elements.OSMBounds;

/** Object to manage multiple readers read from a file */
public record ReadResult(
        PolygonsReader polygons,
        ObjectReader<Dijkstra> dijkstra,
        ObjectReader<AddressDatabase> addresses,
        ObjectReader<OSMBounds> bounds)
        implements AutoCloseable {
    @Override
    public void close() throws Exception {
        polygons.close();
        dijkstra.close();
        addresses.close();
        bounds.close();
    }
}
