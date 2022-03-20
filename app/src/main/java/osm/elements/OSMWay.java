package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMWay extends OSMElement {
    private final SlimOSMWay slim;
    private final List<OSMTag> tags;

    public OSMWay(long id) {
        this(id, new SlimOSMNode[0], new ArrayList<>());
    }

    public OSMWay(long id, SlimOSMNode[] nodes, List<OSMTag> tags) {
        this.slim = new SlimOSMWay(id, nodes);
        this.tags = tags;
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
