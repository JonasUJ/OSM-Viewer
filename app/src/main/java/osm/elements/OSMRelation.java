package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMRelation extends OSMElement {
    private final List<OSMTag> tags = new ArrayList<>();
    private SlimOSMRelation slim;

    public void init(long id) {
        this.slim = new SlimOSMRelation(id, new ArrayList<>());
        tags.clear();
    }

    @Override
    public long id() {
        return slim.id();
    }

    public List<SlimOSMWay> ways() {
        return slim.ways();
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMRelation slim() {
        return slim;
    }
}
