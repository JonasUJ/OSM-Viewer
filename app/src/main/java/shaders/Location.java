// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

/**
 * Represents shader attributes.
 *
 * @author serhiy
 */
public enum Location {
    POSITION("position", LocationType.ATTRIB),
    DRAWABLE_ID("drawable_id", LocationType.ATTRIB),
    PROJECTION("projection", LocationType.UNIFORM),
    COLOR_MAP("color_map", LocationType.UNIFORM),
    MAP("map", LocationType.UNIFORM),
    CATEGORY_BITSET("category_bitset", LocationType.UNIFORM),
    TIME("time", LocationType.UNIFORM);

    private final String name;
    private final LocationType type;

    Location(String name, LocationType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return shader attribute name as it is appearing in the shader source code.
     */
    public String getName() {
        return name;
    }

    public LocationType getType() {
        return type;
    }

    enum LocationType {
        ATTRIB,
        UNIFORM
    }
}
