package io;

import features.Feature;
import osm.elements.OSMBounds;

import java.util.Map;

/**
 * Object to manage multiple readers read from a file
 */
public record ReadResult(Map<Feature, Reader> readers, OSMBounds bounds) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        for (var reader : readers().values()) {
            reader.close();
        }
    }
}
