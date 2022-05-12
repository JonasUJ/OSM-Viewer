package osm.tables;

import collections.ArrayListRefTable;
import osm.OSMObserver;
import osm.elements.OSMRelation;
import osm.elements.SlimOSMRelation;

public class RelationTable extends ArrayListRefTable<SlimOSMRelation> implements OSMObserver {
    @Override
    public void onRelation(OSMRelation relation) {
        put(relation.slim());
    }
}
