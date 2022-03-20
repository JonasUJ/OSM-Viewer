package io;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Reader that reads multiple objects from a stream, but not all at once.
 *
 * @param <T> Type of the objects read
 */
public class StreamingReader<T> extends Reader<Iterable<T>> {

    public StreamingReader(ObjectInputStream in) {
        super(in);
    }

    @Override
    public Iterable<T> read() {
        // An Iterable is anything that returns an Iterator, which this lambda does
        return () ->
                new Iterator<>() {
                    private T obj;

                    @Override
                    public boolean hasNext() {
                        return obj != null || tryRead();
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) throw new NoSuchElementException("next called on empty iterator");

                        // Return the object and set the private field to null *afterwards*
                        // It needs to be null to indicate that we must read another one next time next() is
                        // called
                        try {
                            return obj;
                        } finally {
                            obj = null;
                        }
                    }

                    @SuppressWarnings("unchecked")
                    private boolean tryRead() {
                        try {
                            obj = (T) stream.readUnshared();
                        } catch (EOFException e) {
                            // The only way to know if we're done is to try to read more and see if we fail
                            return false;
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            throw new RuntimeException("error reading object from stream");
                        }

                        return true;
                    }
                };
    }
}
