package osm;

import java.util.List;

enum Tag {
    area(true), building(true), highway(true), natural(true), landuse(true);

    final static List<Tag> values = List.of(values());
    public final boolean drawable;

    Tag(boolean drawable) {
        this.drawable = drawable;
    }

    public static Tag from(String name) {
        if (values.stream().anyMatch(t -> t.name().equals(name))) {
            return valueOf(name);
        }
        return null;
    }
}
