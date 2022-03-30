package geometry;

import java.io.Serializable;

public record Rect(double top, double left, double bottom, double right) implements Serializable {
    public Rect(Point topLeft, Point bottomRight) {
        this(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }

    public Point getTopLeft() {
        return new Point((float) left(), (float) top());
    }

    public Point getTopRight() {
        return new Point((float) right(), (float) top());
    }

    public Point getBottomLeft() {
        return new Point((float) left(), (float) bottom());
    }

    public Point getBottomRight() {
        return new Point((float) right(), (float) bottom());
    }

    public Point center() {
        return new Point((float) (left + right) / 2, (float) (top + bottom) / 2);
    }

    public float distanceTo(Point point) {
        return (float) Math.sqrt(this.distanceSquaredTo(point));
    }

    public double distanceSquaredTo(Point point) {
        double dx = 0;
        double dy = 0;

        if (point.x() < left) dx = point.x() - left;
        else if (point.x() > right) dx = point.x() - right;

        if (point.y() < top) dy = point.y() - top;
        else if (point.y() > bottom) dy = point.y() - bottom;

        return dx * dx + dy * dy;
    }

    public boolean contains(Point point) {
        return point.x() >= left && point.x() <= right && point.y() >= top && point.y() <= bottom;
    }

    public boolean intersects(Rect other) {
        return this.right >= other.left
                && this.bottom >= other.top
                && other.right >= this.left
                && other.bottom >= this.top;
    }
}
