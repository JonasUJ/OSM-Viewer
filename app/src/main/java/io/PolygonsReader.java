package io;

import drawing.Drawing;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Reads a stream written by a PolygonsWriter. The result is an iterable of Drawings, which are read
 * on demand instead of in bulk.
 */
public class PolygonsReader extends StreamingReader<Drawing> {
    private int indexCount = -1;
    private int vertexCount = -1;
    private int drawableCount = -1;

    public PolygonsReader(ObjectInputStream in) {
        super(in);
    }

    /** If not read already, read the count written by the PolygonsWriter */
    private void readCounts() {
        if (indexCount != -1) return;

        try {
            indexCount = stream.readInt();
            vertexCount = stream.readInt();
            drawableCount = stream.readInt();

            // Wrap in another stream to read the 4-byte header
            setStream(new ObjectInputStream(stream));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("error reading counts from stream or reading stream header");
        }
    }

    @Override
    public Iterable<Drawing> read() {
        readCounts();
        return super.read();
    }

    public int getIndexCount() {
        readCounts();
        return indexCount;
    }

    public int getVertexCount() {
        readCounts();
        return vertexCount;
    }

    public int getDrawableCount() {
        readCounts();
        return drawableCount;
    }
}
