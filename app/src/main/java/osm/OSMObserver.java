package osm;

import osm.elements.OSMNode;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

public interface OSMObserver {
    // Don't like default? Sue me.
    default void onNode(OSMNode node) {}

    default void onWay(OSMWay way) {}

    default void onRelation(OSMRelation relation) {}
}
