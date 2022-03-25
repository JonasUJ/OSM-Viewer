package osm.elements;

import collections.Entity;

import java.io.Serializable;

public final class SlimOSMNode extends Entity implements Serializable {
    private final long id;
    private final double lon;
    private final double lat;

    public SlimOSMNode(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public long id() {
        return id;
    }

    public double lon() {
        return lon;
    }

    public double lat() {
        return lat;
    }
}
