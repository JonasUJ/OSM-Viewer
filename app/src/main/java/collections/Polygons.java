package collections;

import com.jogamp.common.nio.Buffers;
import earcut4j.Earcut;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javafx.scene.paint.Color;

public class Polygons implements Serializable {
    FloatList vertices = new FloatList();
    IntList indices = new IntList();
    FloatList colors = new FloatList();

    public Polygons() {}

    // Credit: https://flassari.is/2008/11/line-line-intersection-in-cplusplus/
    static Vector2D intersection(Vector2D p1, Vector2D p2, Vector2D p3, Vector2D p4) {
        // Store the values for fast access and easy
        // equations-to-code conversion
        double x1 = p1.x(), x2 = p2.x(), x3 = p3.x(), x4 = p4.x();
        double y1 = p1.y(), y2 = p2.y(), y3 = p3.y(), y4 = p4.y();

        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        // If d is zero, there is no intersection
        if (Math.abs(d) < 0.01) return null;

        // Get the x and y
        double pre = (x1 * y2 - y1 * x2), post = (x3 * y4 - y3 * x4);
        double x = (pre * (x3 - x4) - (x1 - x2) * post) / d;
        double y = (pre * (y3 - y4) - (y1 - y2) * post) / d;

        // Check if the x and y coordinates are within both lines (I don't need this)
        // if (x < min(x1, x2) || x > max(x1, x2) ||
        // x < min(x3, x4) || x > max(x3, x4)) return null;
        // if (y < min(y1, y2) || y > max(y1, y2) ||
        // y < min(y3, y4) || y > max(y3, y4)) return null;

        // Return the point of intersection
        return new Vector2D(x, y);
    }

    public void addPolygon(List<Vector2D> points, Color color, float layer) {
        var verts = new double[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            var p = points.get(i);
            verts[i * 3] = p.x();
            verts[i * 3 + 1] = p.y();
            verts[i * 3 + 2] = layer;
            addVertex(p, color, layer);
        }

        Earcut.earcut(verts, null, 3).stream()
                .map(i -> (vertices.size() - verts.length) / 3 + i)
                .forEach(indices::add);
    }

    public void addLines(List<Vector2D> points, double width, Color color, float layer) {
        if (points.size() < 2) {
            return;
        } else if (points.size() == 2) {
            addLine(points.get(0), points.get(1), width, color, layer);
            return;
        }

        var from = points.get(0);
        var to = points.get(1);
        var dir = to.sub(from);

        // p0 -- p1
        // |     |
        // p3 -- p2
        var p3 = dir.hat().normalize().scale(width);
        var p0 = p3.scale(-1.0f);
        var p1 = p0.add(dir);
        var p2 = p3.add(dir);

        addVertex(p0.add(from), color, layer);
        addVertex(p3.add(from), color, layer);

        for (int i = 2; i < points.size(); i++) {
            var nextTo = points.get(i);
            var nextDir = nextTo.sub(to);

            var p3Next = nextDir.hat().normalize().scale(width);
            var p0Next = p3Next.scale(-1.0f);
            var p1Next = p0Next.add(nextDir);
            var p2Next = p3Next.add(nextDir);

            indices.add(vertices.size() / 3 - 2);
            indices.add(vertices.size() / 3 + 0);
            indices.add(vertices.size() / 3 + 1);
            indices.add(vertices.size() / 3 - 2);
            indices.add(vertices.size() / 3 + 1);
            indices.add(vertices.size() / 3 - 1);

            var intersect1 = intersection(p0.add(to), p1.add(to), p0Next.add(nextTo), p1Next.add(nextTo));
            var intersect2 = intersection(p3.add(to), p2.add(to), p3Next.add(nextTo), p2Next.add(nextTo));

            if (intersect1 != null) {
                addVertex(intersect1, color, layer);
            } else {
                addVertex(p1.add(to), color, layer);
            }

            if (intersect2 != null) {
                addVertex(intersect2, color, layer);
            } else {
                addVertex(p2.add(to), color, layer);
            }

            to = nextTo;
            p0 = p0Next;
            p1 = p1Next;
            p3 = p3Next;
            p2 = p2Next;
        }

        indices.add(vertices.size() / 3 - 2);
        indices.add(vertices.size() / 3 + 0);
        indices.add(vertices.size() / 3 + 1);
        indices.add(vertices.size() / 3 - 2);
        indices.add(vertices.size() / 3 + 1);
        indices.add(vertices.size() / 3 - 1);

        addVertex(p0.add(to), color, layer);
        addVertex(p3.add(to), color, layer);
    }

    public void addLine(Vector2D from, Vector2D to, double width, Color color, float layer) {
        var vec = from.sub(to);
        var p0 = vec.hat().normalize().scale(width);
        var p3 = p0.scale(-1.0f);
        var p1 = p0.add(vec);
        var p2 = p3.add(vec);

        indices.add(vertices.size() / 3 + 0);
        indices.add(vertices.size() / 3 + 1);
        indices.add(vertices.size() / 3 + 2);
        indices.add(vertices.size() / 3 + 0);
        indices.add(vertices.size() / 3 + 2);
        indices.add(vertices.size() / 3 + 3);

        addVertex(p0.add(from), color, layer);
        addVertex(p1.add(from), color, layer);
        addVertex(p2.add(from), color, layer);
        addVertex(p3.add(from), color, layer);
    }

    void addVertex(Vector2D vertex, Color color, float layer) {
        vertices.add((float) vertex.x());
        vertices.add((float) vertex.y());
        vertices.add(layer);
        addColor(color);
    }

    void addColor(Color color) {
        colors.add((float) color.getRed());
        colors.add((float) color.getGreen());
        colors.add((float) color.getBlue());
    }

    public FloatBuffer getVertexBuffer() {
        return Buffers.newDirectFloatBuffer(vertices.toArray());
    }

    public IntBuffer getIndexBuffer() {
        return Buffers.newDirectIntBuffer(indices.toArray());
    }

    public FloatBuffer getColorBuffer() {
        return Buffers.newDirectFloatBuffer(colors.toArray());
    }
}
