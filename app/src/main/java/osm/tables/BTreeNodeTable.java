package osm.tables;

import collections.Entity;
import collections.btree.BTreeSet;
import collections.btree.Storage;
import osm.OSMObserver;
import osm.elements.OSMNode;

public class BTreeNodeTable extends BTreeSet<Entity> implements OSMObserver {
    public BTreeNodeTable(Storage<Entity> storage) {
        super(storage);
    }

    @Override
    public void onNode(OSMNode node) {
        add(node.slim());
    }
}
