package geometry;

import java.util.ArrayDeque;

public final class Vector2D {
    private static final ArrayDeque<Vector2D> cache = new ArrayDeque<>();

    private double x;
    private double y;

    private Vector2D(double x, double y) {
        init(x, y);
    }

    public static Vector2D create(double x, double y) {
        if (cache.isEmpty()) {
            return new Vector2D(x, y);
        } else {
            var vec = cache.remove();
            vec.init(x, y);
            return vec;
        }
    }

    public static Vector2D create(Point point) {
        return create(point.x(), point.y());
    }

    private void init(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Add the instance back to an internal cache. It is an error to use the Vector2D after calling
     * reuse.
     */
    public void reuse() {
        cache.add(this);
    }

    public Vector2D normalize() {
        return scale(1.0f / magnitude());
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D scale(double s) {
        return create(x * s, y * s);
    }

    public Vector2D hat() {
        return create(-y, x);
    }

    public Vector2D sub(Vector2D other) {
        return create(x - other.x, y - other.y);
    }

    public Vector2D add(Vector2D other) {
        return create(x + other.x, y + other.y);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }
}
