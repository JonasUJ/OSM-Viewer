package osm.elements;

import geometry.Rect;
import java.io.Serializable;
import osm.OSMObserver;

public class OSMBounds implements OSMObserver, Serializable {
    private Rect rect;

    public OSMBounds() {}

    public Rect getRect() {
        return rect;
    }

    @Override
    public void onBounds(Rect bounds) {
        rect = bounds;
    }
}
