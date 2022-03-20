package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMWay;
import osm.elements.SlimOSMWay;

public class WayTable extends RefTable<SlimOSMWay> implements OSMObserver {
    @Override
    public void onWay(OSMWay way) {
        put(way.slim());
    }
}
