package geometry;

public record Line(double ax, double ay, double bx, double by) {
    public Line(Vector2D a, Vector2D b) {
        this(a.x(), a.y(), b.x(), b.y());
    }

    public Vector2D a() {
        return Vector2D.create(ax, ay);
    }

    public Vector2D b() {
        return Vector2D.create(bx, by);
    }

    public Vector2D intersection(Line other) {
        return intersection(this, other);
    }

    public static Vector2D intersection(Line a, Line b) {
        return intersection(a.a(), a.b(), b.a(), b.b());
    }

    // Credit: https://flassari.is/2008/11/line-line-intersection-in-cplusplus/
    public static Vector2D intersection(Vector2D aa, Vector2D ab, Vector2D ba, Vector2D bb) {
        // Store the values for fast access and easy
        // equations-to-code conversion
        double x1 = aa.x(), x2 = ab.x(), x3 = ba.x(), x4 = bb.x();
        double y1 = aa.y(), y2 = ab.y(), y3 = ba.y(), y4 = bb.y();

        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        // If d is zero, there is no intersection
        if (Math.abs(d) < 0.01) return null;

        // Get the x and y
        double pre = (x1 * y2 - y1 * x2), post = (x3 * y4 - y3 * x4);
        double x = (pre * (x3 - x4) - (x1 - x2) * post) / d;
        double y = (pre * (y3 - y4) - (y1 - y2) * post) / d;

        // Return the point of intersection
        return Vector2D.create(x, y);
    }
}
