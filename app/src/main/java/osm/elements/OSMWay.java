package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMWay extends OSMElement {
    private final List<OSMTag> tags = new ArrayList<>();
    private SlimOSMWay slim;

    public void init(long id) {
        this.slim = new SlimOSMWay(id, null);
        tags.clear();
    }

    @Override
    public long id() {
        return slim.id();
    }

    public SlimOSMNode[] nodes() {
        return slim.nodes();
    }

    public void setNodes(SlimOSMNode[] nodes) {
        slim.setNodes(nodes);
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMWay slim() {
        return slim;
    }
}
