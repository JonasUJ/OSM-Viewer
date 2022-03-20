package io;

import collections.Vector2D;
import drawing.Drawable;
import drawing.Drawing;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import osm.elements.OSMMemberWay;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

/** Writes Drawings to a file as they are finished */
public class PolygonsWriter extends TempFileWriter {

    private int indexCount;
    private int vertexCount;
    private int colorCount;
    private Drawing drawing = new Drawing();

    public PolygonsWriter() throws IOException {}

    @Override
    public void writeTo(OutputStream out) throws IOException {
        var objOut = new ObjectOutputStream(out);
        // Write counts to beginning of stream, then write all the drawings
        objOut.writeInt(indexCount);
        objOut.writeInt(vertexCount);
        objOut.writeInt(colorCount);
        super.writeTo(objOut);
    }

    // Write a single Drawing to the stream and forget about it afterwards
    private void writeDrawing() {
        indexCount += drawing.indices().size();
        vertexCount += drawing.vertices().size();
        colorCount += drawing.colors().size();

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

        drawing = new Drawing();
    }

    @Override
    public void onWay(OSMWay way) {
        var drawable = Drawable.from(way);
        if (drawable == Drawable.IGNORED || drawable == Drawable.UNKNOWN) return;

        // Transform nodes to points
        var points = Arrays.stream(way.nodes()).map(n -> new Vector2D(n.lon(), n.lat())).toList();

        switch (drawable.shape) {
            case POLYLINE -> drawing.drawLine(points, drawable, vertexCount / 3);
            case FILL -> drawing.drawPolygon(points, drawable, vertexCount / 3);
        }

        if (drawing.byteSize() >= BUFFER_SIZE) writeDrawing();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRelation(OSMRelation relation) {
        var drawable = Drawable.from(relation);
        if (drawable == Drawable.IGNORED || drawable == Drawable.UNKNOWN) return;

        var lines = new ArrayList<Geometry>();
        var geometryFactory = new GeometryFactory();

        // TODO: Implement ourselves
        // Transform members to line segments
        relation.members().stream()
                .filter(m -> m.role() == OSMMemberWay.Role.OUTER)
                .map(
                        m ->
                                Arrays.stream(m.way().nodes())
                                        .map(n -> new Coordinate(n.lon(), n.lat()))
                                        .toArray(Coordinate[]::new))
                .map(geometryFactory::createLineString)
                .forEach(lines::add);

        var merger = new LineMerger();
        merger.add(lines);
        Collection<LineString> merged = merger.getMergedLineStrings();

        // Merge line segments into one large line and draw it
        drawing.drawPolygon(
                merged.stream()
                        .flatMap(l -> Arrays.stream(l.getCoordinates()).map(c -> new Vector2D(c.x, c.y)))
                        .toList(),
                drawable,
                vertexCount / 3);

        if (drawing.byteSize() >= BUFFER_SIZE) writeDrawing();
    }

    @Override
    public void onFinish() {
        writeDrawing();
    }
}
