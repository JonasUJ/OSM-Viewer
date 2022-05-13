package osm.elements;

import geometry.Rect;
import osm.OSMObserver;

import java.io.Serializable;

public class OSMBounds implements OSMObserver, Serializable {
    private Rect rect;

    public OSMBounds() {
    }

    public Rect getRect() {
        return rect;
    }

    @Override
    public void onBounds(Rect bounds) {
        rect = bounds;
    }
}
