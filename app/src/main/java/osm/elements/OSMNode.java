package osm.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class OSMNode extends OSMElement implements Serializable {
    private final List<OSMTag> tags = new ArrayList<>();
    private SlimOSMNode slim;

    public void init(long id, double lon, double lat) {
        this.slim = new SlimOSMNode(id, lon, lat);
        tags.clear();
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
