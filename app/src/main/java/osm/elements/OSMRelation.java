package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMRelation extends OSMElement {
    private final SlimOSMRelation slim;
    private final List<OSMTag> tags;

    public OSMRelation(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public OSMRelation(long id, List<OSMMemberWay> members, List<OSMTag> tags) {
        this.slim = new SlimOSMRelation(id, members);
        this.tags = tags;
    }

    @Override
    public long id() {
        return slim.id();
    }

    public List<OSMMemberWay> members() {
        return slim.members();
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMRelation slim() {
        return slim;
    }
}
