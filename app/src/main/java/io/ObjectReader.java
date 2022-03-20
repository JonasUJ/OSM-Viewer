package io;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Reads a single object from the input stream
 *
 * @param <T> Type of the object to read
 */
public class ObjectReader<T> extends Reader<T> {

    public ObjectReader(ObjectInputStream in) {
        super(in);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read() {
        try {
            return (T) stream.readUnshared();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("error reading object from stream");
        }
    }
}
