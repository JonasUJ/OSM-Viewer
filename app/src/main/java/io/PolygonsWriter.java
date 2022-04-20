package io;

import drawing.*;
import geometry.Point;
import geometry.Rect;
import geometry.Vector2D;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import osm.elements.*;

/** Writes Drawings to a file as they are finished */
public class PolygonsWriter extends TempFileWriter {

    private int indexCount;
    private int vertexCount;
    private int drawableCount;
    private final DrawingManager manager = new DrawingManager();

    public PolygonsWriter() throws IOException {}

    @Override
    public void writeTo(OutputStream out) throws IOException {
        var objOut = new ObjectOutputStream(out);
        // Write counts to beginning of stream, then write all the drawings
        objOut.writeInt(indexCount);
        objOut.writeInt(vertexCount);
        objOut.writeInt(drawableCount);
        super.writeTo(objOut);
    }

    // Write a single Drawing to the stream and forget about it afterwards
    private void writeDrawing() {
        var drawing = manager.drawing();

        indexCount += drawing.indices().size();
        vertexCount += drawing.vertices().size();
        drawableCount += drawing.drawables().size();

        try {
            stream.writeUnshared(drawing);
            stream.flush();
            stream.reset();
        } catch (IOException e) {
            // We can't have checked exceptions here because this method is called from overwritten
            // methods, and we don't want to change their signature.
            // TODO: Handle >_>
            e.printStackTrace();
            throw new RuntimeException("could not write drawing to stream");
        }

        manager.clear();
    }

    @Override
    public void onBounds(Rect bounds) {
        manager.draw(
                List.of(
                        new Vector2D(Point.geoToMap(bounds.getTopLeft())),
                        new Vector2D(Point.geoToMap(bounds.getTopRight())),
                        new Vector2D(Point.geoToMap(bounds.getBottomRight())),
                        new Vector2D(Point.geoToMap(bounds.getBottomLeft())),
                        new Vector2D(Point.geoToMap(bounds.getTopLeft()))),
                Drawable.BOUNDS);
    }

    @Override
    public void onWay(OSMWay way) {
        var drawable = Drawable.from(way);
        if (drawable == Drawable.IGNORED || drawable == Drawable.UNKNOWN) return;

        // Transform nodes to points
        var points =
                Arrays.stream(way.nodes())
                        .map(n -> new Vector2D(Point.geoToMapX(n.lon()), Point.geoToMapY(n.lat())))
                        .toList();

        manager.drawOrdered(points, drawable, vertexCount / 2);

        if (manager.byteSize() >= BUFFER_SIZE) writeDrawing();
    }

    @Override
    public void onRelation(OSMRelation relation) {
        var drawable = Drawable.from(relation);
        if (drawable == Drawable.IGNORED || drawable == Drawable.UNKNOWN) return;

        // Create line segments from all members and join them
        var joiner =
                new SegmentJoiner<>(
                        relation.members().stream()
                                .filter(m -> m.role() == OSMMemberWay.Role.OUTER)
                                .map(OSMMemberWay::way)
                                .map(SlimOSMWay::nodes)
                                .map(Arrays::asList)
                                .map(Segment<SlimOSMNode>::new)
                                .toList());
        joiner.join();

        // Draw all the segments
        for (var segment : joiner) {
            manager.drawOrdered(
                    segment.stream()
                            .map(n -> new Vector2D(Point.geoToMapX(n.lon()), Point.geoToMapY(n.lat())))
                            .toList(),
                    drawable,
                    vertexCount / 2);
        }

        if (manager.byteSize() >= BUFFER_SIZE) writeDrawing();
    }

    @Override
    public void onFinish() {
        writeDrawing();
    }
}
