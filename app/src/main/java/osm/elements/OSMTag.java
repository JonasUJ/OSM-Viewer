package osm.elements;

import java.io.Serializable;

public record OSMTag(Key key, String value) implements Serializable {
    public static OSMTag from(String key, String value) {
        var k = Key.from(key);
        if (k == null) return null;

        return new OSMTag(k, value);
    }

    public enum Key {
        BUILDING,
        HIGHWAY,
        WATERWAY,
        CYCLEWAY,
        CYCLEWAY_LEFT,
        CYCLEWAY_RIGHT,
        CYCLEWAY_BOTH,
        FOOTWAY,
        SERVICE,
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
        JUNCTION,
        ONEWAY,
        MAXSPEED,
        STREET,
        HOUSENUMBER,
        CITY,
        POSTCODE;

        public static Key from(String key) {
            return switch (key) {
                case "building" -> BUILDING;
                case "highway" -> HIGHWAY;
                case "waterway" -> WATERWAY;
                case "cycleway" -> CYCLEWAY;
                case "cycleway:left" -> CYCLEWAY_LEFT;
                case "cycleway:right" -> CYCLEWAY_RIGHT;
                case "cycleway:both" -> CYCLEWAY_BOTH;
                case "footway" -> FOOTWAY;
                case "service" -> SERVICE;
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
                case "junction" -> JUNCTION;
                case "oneway" -> ONEWAY;
                case "maxspeed" -> MAXSPEED;
                case "addr:street" -> STREET;
                case "addr:housenumber" -> HOUSENUMBER;
                case "addr:city" -> CITY;
                case "addr:postcode" -> POSTCODE;
                default -> null;
            };
        }
    }
}
