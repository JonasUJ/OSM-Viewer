package io;

import collections.grid.Grid;
import drawing.*;
import geometry.Point;
import geometry.Rect;
import geometry.Vector2D;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;
import osm.elements.SlimOSMNode;
import osm.elements.SlimOSMWay;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Writes Drawings to a file as they are finished
 */
public class PolygonsWriter extends TempFileWriter {
    private static final int MAX_SIZE = 100 * 1024 * 1024; // Max size on heap before flushing chunks
    private static final float CELL_SIZE = 0.05f;
    private static final int GRIDS = 3;

    private final List<Vector2D> points = new ArrayList<>();
    private final PartialChunk baseChunk = new PartialChunk(null, 0);
    private final List<Grid<PartialChunk>> grids = new ArrayList<>();
    private int maxChunkSize;
    private Rect bounds;

    public PolygonsWriter() throws IOException {
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        var objOut = new ObjectOutputStream(out);

        // Write header to beginning of stream
        objOut.writeUnshared(bounds);
        objOut.writeUnshared(baseChunk);
        objOut.writeInt(grids.size());
        for (var grid : grids) {
            objOut.writeFloat(grid.cellSize);
            objOut.writeInt(grid.size());
            for (var chunk : grid) {
                objOut.writeUnshared(chunk.point);
                objOut.writeInt(chunk.getTotalIndices());
                objOut.writeInt(chunk.getTotalVertices());
                objOut.writeInt(chunk.getTotalDrawables());
            }
        }

        // Write chunks
        super.writeTo(objOut);
    }

    @Override
    public void onBounds(Rect bounds) {
        this.bounds = bounds;
        var total = 0;
        for (int i = 1; i <= GRIDS; i++) {
            var cellSize = (int) Math.pow(i, i) * CELL_SIZE;
            var grid = new Grid<>(bounds, cellSize, p -> new PartialChunk(p, cellSize));
            total += grid.size();
            grids.add(grid);
        }
        maxChunkSize = MAX_SIZE / total;

        baseChunk.add(
                Drawing.create(
                        List.of(
                                Vector2D.create(Point.geoToMap(bounds.getTopLeft())),
                                Vector2D.create(Point.geoToMap(bounds.getTopRight())),
                                Vector2D.create(Point.geoToMap(bounds.getBottomRight())),
                                Vector2D.create(Point.geoToMap(bounds.getBottomLeft())),
                                Vector2D.create(Point.geoToMap(bounds.getTopLeft()))),
                        DrawableEnum.BOUNDS));
    }

    @Override
    public void onWay(OSMWay way) {
        var drawable = DrawableEnum.from(way);
        if (drawable == DrawableEnum.IGNORED || drawable == DrawableEnum.UNKNOWN) return;

        drawNodes(Arrays.asList(way.nodes()), drawable);
    }

    @Override
    public void onRelation(OSMRelation relation) {
        var drawable = DrawableEnum.from(relation);
        if (drawable == DrawableEnum.IGNORED || drawable == DrawableEnum.UNKNOWN) return;

        // Create line segments from all members and join them
        var joiner =
                new SegmentJoiner<>(
                        relation.ways().stream()
                                .map(SlimOSMWay::nodes)
                                .map(Arrays::asList)
                                .map(Segment<SlimOSMNode>::new)
                                .toList());
        joiner.join();

        // Draw all the segments
        for (var segment : joiner) {
            drawNodes(segment, drawable);
        }
    }

    private void drawNodes(Iterable<SlimOSMNode> iter, Drawable drawable) {
        // Transform nodes to points and get bounding box
        double top = Double.POSITIVE_INFINITY,
                left = Double.POSITIVE_INFINITY,
                bottom = Double.NEGATIVE_INFINITY,
                right = Double.NEGATIVE_INFINITY;
        for (var node : iter) {
            points.add(Vector2D.create(Point.geoToMapX(node.lon()), Point.geoToMapY(node.lat())));
            if (node.lon() < left) left = node.lon();
            if (node.lon() > right) right = node.lon();
            if (node.lat() < top) top = node.lat();
            if (node.lat() > bottom) bottom = node.lat();
        }

        // If the element spans many cells, or it has high detail, we add it to the base instead of
        // duplicating it across them all.
        var size = (right - left) + (bottom - top);
        if ((size > 2 * CELL_SIZE || drawable.detail() >= GRIDS - 1 && size > 0.3 * CELL_SIZE)
                && drawable.shape() == Drawable.Shape.FILL) {
            var drawing = Drawing.create(points, drawable);
            baseChunk.add(drawing);
        } else {
            for (int detail = 0; detail < grids.size(); detail++) {
                if (detail > drawable.detail()) break;

                var drawing = Drawing.create(points, DrawableDetailWrapper.from(drawable, detail));

                var grid = grids.get(detail);
                for (var chunk : grid.range(top, left, bottom, right)) {
                    if (chunk == null) continue;

                    chunk.add(drawing);

                    if (chunk.byteSize() > maxChunkSize) {
                        chunk.flush(stream);
                    }
                }
            }
        }

        points.forEach(Vector2D::reuse);
        points.clear();
    }

    @Override
    public void onFinish() {
        for (var grid : grids) {
            for (var chunk : grid) {
                chunk.flush(stream);
            }
        }
    }
}
