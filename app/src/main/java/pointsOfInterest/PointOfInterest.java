package pointsOfInterest;

import collections.Entity;

public final class PointOfInterest {
    private final float lon;
    private final float lat;
    private final String name;
    private Entity drawing;

    public PointOfInterest(float lon, float lat, String name) {
        this.lon = lon;
        this.lat = lat;
        this.name = name;
    }

    public float lon() {
        return lon;
    }

    public float lat() {
        return lat;
    }

    public String name() {
        return name;
    }

    public Entity getDrawing() {
        return drawing;
    }

    public void setDrawing(Entity drawing) {
        this.drawing = drawing;
    }
}
