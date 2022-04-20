package geometry;

import java.io.Serializable;

public record Rect(float top, float left, float bottom, float right) implements Serializable {
    public Rect(Point topLeft, Point bottomRight) {
        this(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }

    public Point getTopLeft() {
        return new Point(left(), top());
    }

    public Point getTopRight() {
        return new Point(right(), top());
    }

    public Point getBottomLeft() {
        return new Point(left(), bottom());
    }

    public Point getBottomRight() {
        return new Point(right(), bottom());
    }

    public Point center() {
        return new Point((left + right) / 2, (top + bottom) / 2);
    }

    public float distanceTo(Point point) {
        return (float) Math.sqrt(this.distanceSquaredTo(point));
    }

    public float distanceSquaredTo(Point point) {
        float dx = 0;
        float dy = 0;

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
