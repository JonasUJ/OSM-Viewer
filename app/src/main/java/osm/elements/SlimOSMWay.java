package osm.elements;

import collections.Entity;

public final class SlimOSMWay extends Entity {
    private final long id;
    private SlimOSMNode[] nodes;

    public SlimOSMWay(long id, SlimOSMNode[] nodes) {
        this.id = id;
        this.nodes = nodes;
    }

    @Override
    public long id() {
        return id;
    }

    public SlimOSMNode[] nodes() {
        return nodes;
    }

    void setNodes(SlimOSMNode[] nodes) {
        this.nodes = nodes;
    }
}
