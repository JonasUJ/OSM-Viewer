package geometry;

import javafx.geometry.Point2D;

public record Point(float x, float y) {
    public Point(Point2D point) {
        this((float) point.getX(), (float) point.getY());
    }

    public Point(Vector2D vec) {
        this((float) vec.x(), (float) vec.y());
    }

    public float distanceTo(Point other) {
        return (float) Math.sqrt(distanceSquaredTo(other));
    }

    public float distanceSquaredTo(Point other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;

        return dx * dx + dy * dy;
    }

    // TODO: Find equations that can translate sphere to a flat earth
    public static Point geoToMap(Point point) {
        return new Point((float) geoToMapX(point.x()), (float) geoToMapY(point.y()));
    }

    public static double geoToMapX(double x) {
        return (x - 18) * 5600;
    }

    public static double geoToMapY(double y) {
        return -(y - 56) * 10000;
    }

    public static Point mapToGeo(Point point) {
        return new Point((float) mapToGeoX(point.x()), (float) mapToGeoY(point.y()));
    }

    public static double mapToGeoX(double x) {
        return x / 5600 + 18;
    }

    public static double mapToGeoY(double y) {
        return -y / 10000 + 56;
    }

    public Point2D point2D() {
        return new Point2D(x(), y());
    }
}
