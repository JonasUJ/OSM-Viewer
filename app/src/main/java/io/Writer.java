package io;

import java.io.IOException;
import java.io.OutputStream;
import osm.OSMObserver;

/** Represents a class that can be written to an output stream */
public interface Writer extends OSMObserver {
    void writeTo(OutputStream out) throws IOException;
}
