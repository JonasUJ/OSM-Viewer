package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMRelation;
import osm.elements.SlimOSMRelation;

public class RelationTable extends RefTable<SlimOSMRelation> implements OSMObserver {
    @Override
    public void onRelation(OSMRelation relation) {
        put(relation.slim());
    }
}
