package osm.tables;

import collections.ArrayListRefTable;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.SlimOSMNode;

public class NodeTable extends ArrayListRefTable<SlimOSMNode> implements OSMObserver {
    @Override
    public void onNode(OSMNode node) {
        put(node.slim());
    }
}
