package osm.elements;

import java.io.Serializable;

public record OSMTag(Key key, String value) implements Serializable {
    public enum Key {
        BUILDING,
        HIGHWAY,
        NATURAL,
        LANDUSE,
        AMENITY,
        LEISURE,
        BARRIER,
        TOURISM,
        MAN_MADE,
        PLACE,
        TYPE,
        NAME,
        MAXSPEED,
        STREET,
        HOUSENUMBER,
        CITY,
        POSTCODE;

        public static Key from(String key) {
            return switch (key) {
                case "building" -> BUILDING;
                case "highway" -> HIGHWAY;
                case "natural" -> NATURAL;
                case "landuse" -> LANDUSE;
                case "amenity" -> AMENITY;
                case "leisure" -> LEISURE;
                case "barrier" -> BARRIER;
                case "tourism" -> TOURISM;
                case "man_made" -> MAN_MADE;
                case "place" -> PLACE;
                case "type" -> TYPE;
                case "name" -> NAME;
                case "maxspeed" -> MAXSPEED;
                case "addr:street" -> STREET;
                case "addr:housenumber" -> HOUSENUMBER;
                case "addr:city" -> CITY;
                case "addr:postcode" -> POSTCODE;
                default -> null;
            };
        }
    }

    public static OSMTag from(String key, String value) {
        var k = Key.from(key);
        if (k == null) return null;

        return new OSMTag(k, value);
    }
}
