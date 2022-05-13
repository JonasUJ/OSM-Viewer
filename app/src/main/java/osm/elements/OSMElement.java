package osm.elements;

import collections.Entity;

import java.util.List;

public abstract class OSMElement extends Entity {
    public abstract List<OSMTag> tags();
}
