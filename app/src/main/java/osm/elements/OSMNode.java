package osm.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class OSMNode extends OSMElement implements Serializable {
    private final SlimOSMNode slim;
    private final List<OSMTag> tags;

    public OSMNode(long id, double lon, double lat) {
        this(id, lon, lat, new ArrayList<>());
    }

    public OSMNode(long id, double lon, double lat, List<OSMTag> tags) {
        this.slim = new SlimOSMNode(id, lon, lat);
        this.tags = tags;
    }

    @Override
    public long id() {
        return slim.id();
    }

    public double lon() {
        return slim.lon();
    }

    public double lat() {
        return slim.lat();
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMNode slim() {
        return slim;
    }
}
