package io;

import features.Feature;
import java.util.Map;
import osm.elements.OSMBounds;

/** Object to manage multiple readers read from a file */
public record ReadResult(Map<Feature, Reader> readers, OSMBounds bounds) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        for (var reader : readers().values()) {
            reader.close();
        }
    }
}
