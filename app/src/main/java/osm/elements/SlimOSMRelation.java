package osm.elements;

import collections.Entity;
import java.util.List;

public final class SlimOSMRelation extends Entity {
    private final long id;
    private final List<SlimOSMWay> ways;

    public SlimOSMRelation(long id, List<SlimOSMWay> ways) {
        this.id = id;
        this.ways = ways;
    }

    @Override
    public long id() {
        return id;
    }

    public List<SlimOSMWay> ways() {
        return ways;
    }
}
