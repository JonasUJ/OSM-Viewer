package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.SlimOSMNode;

public class NodeTable extends RefTable<SlimOSMNode> implements OSMObserver {
    @Override
    public void onNode(OSMNode node) {
        put(node.slim());
    }
}
