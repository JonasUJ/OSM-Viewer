package io;

import java.io.ObjectInputStream;

/**
 * Reads objects from a stream
 *
 * @param <T> Type of the object read
 */
public abstract class Reader<T> implements AutoCloseable {
    protected ObjectInputStream stream;

    public Reader(ObjectInputStream in) {
        setStream(in);
    }

    public abstract T read();

    /**
     * Set the stream to read from
     *
     * @param in New stream
     */
    protected void setStream(ObjectInputStream in) {
        stream = in;
    }

    @Override
    public void close() throws Exception {
        stream.close();
    }
}
