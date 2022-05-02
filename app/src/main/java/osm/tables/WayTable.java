package osm.tables;

import collections.ArrayListRefTable;
import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMWay;
import osm.elements.SlimOSMWay;

public class WayTable extends ArrayListRefTable<SlimOSMWay> implements OSMObserver {
    @Override
    public void onWay(OSMWay way) {
        put(way.slim());
    }
}
