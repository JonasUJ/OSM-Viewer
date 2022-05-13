package io;

import canvas.Chunk;
import geometry.Point;
import geometry.Rect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a stream written by a PolygonsWriter. The result is an iterable of PartialChunks, which are
 * read on demand instead of in bulk.
 */
public class PolygonsReader extends StreamingReader<PartialChunk> {
    private Map<Float, Map<Point, Chunk>> chunks = null;
    private Rect bounds;
    private PartialChunk baseChunk;

    public PolygonsReader(ObjectInputStream in) {
        super(in);
    }

    /**
     * If not read already, read the header written by the PolygonsWriter
     */
    private void readHeader() {
        if (chunks != null) return;

        chunks = new HashMap<>();

        try {
            bounds = (Rect) stream.readUnshared();
            baseChunk = (PartialChunk) stream.readUnshared();

            var gridCount = stream.readInt();
            for (int i = 0; i < gridCount; i++) {
                var cellSize = stream.readFloat();
                var chunkCount = stream.readInt();
                var map = new HashMap<Point, Chunk>();

                for (int j = 0; j < chunkCount; j++) {
                    var p = (Point) stream.readUnshared();
                    var chunk = new Chunk(stream.readInt(), stream.readInt(), stream.readInt());

                    map.put(p, chunk);
                }

                chunks.put(cellSize, map);
            }

            // Wrap in another stream to read the 4-byte object stream header
            setStream(new ObjectInputStream(stream));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<PartialChunk> read() {
        readHeader();
        return super.read();
    }

    public Map<Float, Map<Point, Chunk>> getChunks() {
        readHeader();
        return chunks;
    }

    public Rect getBounds() {
        readHeader();
        return bounds;
    }

    public PartialChunk getBaseChunk() {
        readHeader();
        return baseChunk;
    }
}
