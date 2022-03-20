package osm.elements;

import collections.Entity;
import java.util.List;

public final class SlimOSMRelation extends Entity {
    private final long id;
    private final List<OSMMemberWay> members;

    public SlimOSMRelation(long id, List<OSMMemberWay> members) {
        this.id = id;
        this.members = members;
    }

    @Override
    public long id() {
        return id;
    }

    public List<OSMMemberWay> members() {
        return members;
    }
}
