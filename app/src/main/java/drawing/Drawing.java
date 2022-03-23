package drawing;

import geometry.Line;
import geometry.Vector2D;
import collections.lists.ByteList;
import collections.lists.FloatList;
import collections.lists.IntList;
import earcut4j.Earcut;
import java.io.*;
import java.util.List;

/** A Drawing represents drawn elements in a format that can be easily passed to OpenGL */
public class Drawing implements Serializable {
    private IntList indices;
    private FloatList vertices;
    private ByteList colors;

    public Drawing() {
        this(new IntList(), new FloatList(), new ByteList());
    }

    public Drawing(IntList indices, FloatList vertices, ByteList colors) {
        this.indices = indices;
        this.vertices = vertices;
        this.colors = colors;
    }

    public void drawPolygon(List<Vector2D> points, Drawable drawable) {
        drawPolygon(points, drawable, 0);
    }

    public void drawPolygon(List<Vector2D> points, Drawable drawable, int offset) {
        drawPolygon(points, drawable.mapColor, drawable.layer(), offset);
    }

    public void drawPolygon(List<Vector2D> points, MapColor mapColor, float layer, int offset) {
        // Copy all coordinates into an array
        var verts = new double[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            var p = points.get(i);
            verts[i * 3] = p.x();
            verts[i * 3 + 1] = p.y();
            verts[i * 3 + 2] = layer;
            addVertex(p, mapColor, layer);
        }

        // Calculate indices for each vertex in triangulated polygon
        Earcut.earcut(verts, null, 3).stream()
                // Offset each index before adding to indices
                .map(i -> (vertices().size() - verts.length) / 3 + offset + i)
                .forEach(indices()::add);
    }

    public void drawLine(List<Vector2D> points, Drawable drawable) {
        drawLine(points, drawable, 0);
    }

    public void drawLine(List<Vector2D> points, Drawable drawable, int offset) {
        drawLine(points, drawable.size, drawable.mapColor, drawable.layer(), offset);
    }

    public void drawLine(
            List<Vector2D> points, double width, MapColor mapColor, float layer, int offset) {
        // Lines must exist of at least two points
        if (points.size() < 2) {
            return;
        }

        // To draw a line with triangles, we must find p0-3 and connect them accordingly.
        // Diagram showing what each variable corresponds to:
        //  p0 ------------ p1
        //   |               |
        // from --- dir --> to
        //   |               |
        //  p3 ------------ p2

        var from = points.get(0);
        var to = points.get(1);
        var dir = to.sub(from);

        // find p0-3 by manipulating vectors
        var p3 = dir.hat().normalize().scale(width);
        var p0 = p3.scale(-1.0f);
        var p1 = p0.add(dir);
        var p2 = p3.add(dir);

        // These points are final, we can add them now
        addVertex(p0.add(from), mapColor, layer);
        addVertex(p3.add(from), mapColor, layer);

        // Loop through remaining points in line, calculating a pair of points in each iteration
        for (int i = 2; i < points.size(); i++) {
            // Where we're going next
            var nextTo = points.get(i);
            var nextDir = nextTo.sub(to);

            // Corners drawn from the next point
            var p3Next = nextDir.hat().normalize().scale(width);
            var p0Next = p3Next.scale(-1.0f);
            var p1Next = p0Next.add(nextDir);
            var p2Next = p3Next.add(nextDir);

            addLineIndices(offset);

            // Find intersections between previous two lines and next two lines
            var intersect1 = Line.intersection(p0.add(to), p1.add(to), p0Next.add(nextTo), p1Next.add(nextTo));
            var intersect2 = Line.intersection(p3.add(to), p2.add(to), p3Next.add(nextTo), p2Next.add(nextTo));

            // Intersection is null if lines are parallel
            if (intersect1 != null) {
                addVertex(intersect1, mapColor, layer);
            } else {
                addVertex(p1.add(to), mapColor, layer);
            }

            if (intersect2 != null) {
                addVertex(intersect2, mapColor, layer);
            } else {
                addVertex(p2.add(to), mapColor, layer);
            }

            // Move forward one point, setting the "current" points to the "next points"
            to = nextTo;
            p0 = p0Next;
            p1 = p1Next;
            p3 = p3Next;
            p2 = p2Next;
        }

        addLineIndices(offset);

        addVertex(p0.add(to), mapColor, layer);
        addVertex(p3.add(to), mapColor, layer);
    }

    private void addLineIndices(int offset) {
        var size = offset + vertices().size() / 3;

        // Add two triangles
        indices().add(size - 2);
        indices().add(size + 0);
        indices().add(size + 1);
        indices().add(size - 2);
        indices().add(size + 1);
        indices().add(size - 1);
    }

    /** Add a vertex with a color and layer into the correct position in `vertices` and `colors` */
    private void addVertex(Vector2D vertex, MapColor mapColor, float layer) {
        vertices().add((float) vertex.x());
        vertices().add((float) vertex.y());
        vertices().add(layer);
        colors().add(mapColor.colorIdx());
    }

    public int byteSize() {
        return indices.size() * Integer.BYTES
                + vertices.size() * Float.BYTES
                + colors.size() * Float.BYTES;
    }

    public IntList indices() {
        return indices;
    }

    public FloatList vertices() {
        return vertices;
    }

    public ByteList colors() {
        return colors;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        indices = (IntList) in.readUnshared();
        vertices = (FloatList) in.readUnshared();
        colors = (ByteList) in.readUnshared();
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUnshared(indices);
        out.writeUnshared(vertices);
        out.writeUnshared(colors);
    }
}
