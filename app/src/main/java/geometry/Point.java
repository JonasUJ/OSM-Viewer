package geometry;

public record Point(float x, float y) {
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
}
