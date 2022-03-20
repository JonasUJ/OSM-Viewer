package drawing;

import osm.elements.OSMElement;
import osm.elements.OSMTag;

// TODO: Missing Area
public enum Drawable {
    ISLAND(Shape.FILL, MapColor.ISLAND, 0),
    BEACH(Shape.FILL, MapColor.BEACH, 0),
    SAND(Shape.FILL, MapColor.SAND, 0),
    FARMLAND(Shape.FILL, MapColor.FARMLAND, 0),
    FARMYARD(Shape.FILL, MapColor.FARMYARD, 0),
    FOREST(Shape.FILL, MapColor.FOREST, 0),
    RESIDENTIAL(Shape.FILL, MapColor.RESIDENTIAL, 0),
    PARK(Shape.FILL, MapColor.PARK, 0),
    CONSTRUCTION(Shape.FILL, MapColor.CONSTRUCTION, 0),
    CAMP_SITE(Shape.FILL, MapColor.CAMP_SITE, 0),
    BREAK_WATER(Shape.FILL, MapColor.BREAK_WATER, 0),
    PARKING(Shape.FILL, MapColor.PARKING, 0),
    ALLOTMENTS(Shape.FILL, MapColor.ALLOTMENTS, 0),
    LEISURE_PARK(Shape.FILL, MapColor.LEISURE_PARK, 0),
    GRASS(Shape.FILL, MapColor.GRASS, 0),
    GOLF(Shape.FILL, MapColor.GOLF, 0),
    SPORTS_CENTRE(Shape.FILL, MapColor.SPORTS_CENTRE, 0),
    AMENITY_AREA(Shape.FILL, MapColor.AMENITY_AREA, 0),
    HEATH(Shape.FILL, MapColor.HEATH, 0),
    BARE_ROCK(Shape.FILL, MapColor.BARE_ROCK, 0),
    QUARRY(Shape.FILL, MapColor.QUARRY, 0),
    SCRUB(Shape.FILL, MapColor.SCRUB, 0),
    ORCHARD(Shape.FILL, MapColor.ORCHARD, 0),
    INDUSTRIAL(Shape.FILL, MapColor.INDUSTRIAL, 0),
    PLANT_NURSERY(Shape.FILL, MapColor.PLANT_NURSERY, 0),
    WATER(Shape.FILL, MapColor.WATER, 0),
    MUSEUM(Shape.FILL, MapColor.MUSEUM, 0),
    BUILDING(Shape.FILL, MapColor.BUILDING, 0),
    REST_AREA(Shape.FILL, MapColor.REST_AREA, 0),
    DWELLING(Shape.FILL, MapColor.DWELLING, 0),
    PITCH(Shape.FILL, MapColor.PITCH, 0),
    RETAIL(Shape.FILL, MapColor.RETAIL, 0),
    PRISON(Shape.FILL, MapColor.PRISON, 0),
    CLIFF(Shape.POLYLINE, MapColor.CLIFF, 0.1),
    HEDGE(Shape.POLYLINE, MapColor.HEDGE, 0.05),
    WALL(Shape.POLYLINE, MapColor.WALL, 0.05),
    PIER(Shape.POLYLINE, MapColor.PIER, 0.05),
    PATH(Shape.POLYLINE, MapColor.PATH, 0.1),
    TRACK(Shape.POLYLINE, MapColor.TRACK, 0.2),
    SERVICE(Shape.POLYLINE, MapColor.SERVICE, 0.2),
    ROAD(Shape.POLYLINE, MapColor.ROAD, 0.4),
    TREE_ROW(Shape.POLYLINE, MapColor.TREE_ROW, 0.2),
    NATURE_RESERVE(Shape.POLYLINE, MapColor.NATURE_RESERVE, 0.2), // TODO: Translucent?
    TERTIARY(Shape.POLYLINE, MapColor.TERTIARY, 0.7),
    SECONDARY(Shape.POLYLINE, MapColor.SECONDARY, 0.8),
    PRIMARY(Shape.POLYLINE, MapColor.PRIMARY, 0.9),
    MOTORWAY(Shape.POLYLINE, MapColor.MOTORWAY, 1.0),
    UNKNOWN(Shape.FILL, MapColor.UNKNOWN, 0),
    IGNORED(Shape.FILL, MapColor.IGNORED, 0);

    public static final float length = values().length;

    public final Shape shape;
    public final MapColor mapColor;
    public final double size;

    Drawable(Shape shape, MapColor mapColor, double size) {
        this.shape = shape;
        this.mapColor = mapColor;
        this.size = size;
    }

    public float layer() {
        return ordinal() / length;
    }

    private static Drawable _default(OSMTag tag) {
        // System.out.println("Unknown tag " + tag.toString());
        return UNKNOWN;
    }

    public static Drawable from(OSMElement element) {
        var drawable = Drawable.UNKNOWN;
        for (var tag : element.tags()) {
            drawable = Drawable.from(tag);
            if (drawable != Drawable.UNKNOWN) break;
        }
        return drawable;
    }

    public static Drawable from(OSMTag tag) {
        if (tag == null) return UNKNOWN;
        return switch (tag.key()) {
            case LANDUSE -> switch (tag.value()) {
                case "allotments" -> ALLOTMENTS;
                case "plant_nursery" -> PLANT_NURSERY;
                case "basin" -> WATER;
                case "cemetery", "park", "village_green", "vineyard" -> PARK;
                case "residential", "commercial" -> RESIDENTIAL;
                case "industrial" -> INDUSTRIAL;
                case "recreation_ground" -> LEISURE_PARK;
                case "construction" -> CONSTRUCTION;
                case "farmland", "greenhouse_horticulture" -> FARMLAND;
                case "farmyard" -> FARMYARD;
                case "orchard" -> ORCHARD;
                case "quarry" -> QUARRY;
                case "military", "greenfield" -> IGNORED;
                case "retail" -> RETAIL;
                case "isolated_dwelling" -> DWELLING;
                case "grass", "meadow" -> GRASS;
                case "tree", "forest", "wood", "green_field" -> FOREST;
                default -> _default(tag);
            };
            case NATURAL -> switch (tag.value()) {
                case "wood" -> FOREST;
                case "water" -> WATER;
                case "cliff" -> CLIFF;
                case "scrub" -> SCRUB;
                case "tree_row" -> TREE_ROW;
                case "grassland" -> GRASS;
                case "bare_rock" -> BARE_ROCK;
                case "valley" -> IGNORED;
                case "wetland", "heath" -> HEATH;
                case "beach" -> BEACH;
                case "sand" -> SAND;
                default -> _default(tag);
            };
            case AMENITY -> switch (tag.value()) {
                case "toilets", "shelter", "biergarten" -> BUILDING;
                case "grave_yard" -> PARK;
                case "prison" -> PRISON;
                case "recycling" -> INDUSTRIAL;
                case "parking", "bicycle_parking", "parking_space", "bus_station", "taxi" -> PARKING;
                case "school", "kindergarten", "hospital" -> AMENITY_AREA;
                default -> _default(tag);
            };
            case LEISURE -> switch (tag.value()) {
                case "beach_resort" -> SAND;
                case "park" -> PARK;
                case "track" -> TRACK;
                case "sports_centre", "stadium" -> SPORTS_CENTRE;
                case "playground", "water_park" -> LEISURE_PARK;
                case "pitch" -> PITCH;
                case "marina", "swimming_pool" -> WATER;
                case "garden" -> GRASS;
                case "nature_reserve" -> NATURE_RESERVE;
                case "miniature_golf", "golf_course" -> GOLF;
                default -> _default(tag);
            };
            case BUILDING -> BUILDING;
            case BARRIER -> switch (tag.value()) {
                case "yes" -> IGNORED;
                case "hedge" -> HEDGE;
                case "wall", "fence", "retaining_wall" -> WALL;
                default -> _default(tag);
            };
            case TOURISM -> switch (tag.value()) {
                case "camp_site" -> CAMP_SITE;
                case "museum" -> MUSEUM;
                default -> _default(tag);
            };
            case MAN_MADE -> switch (tag.value()) {
                case "pier" -> PIER;
                case "storage_tank", "silo" -> BUILDING;
                case "breakwater" -> BREAK_WATER;
                case "bunker_silo" -> IGNORED;
                case "wastewater_plant", "water_works" -> INDUSTRIAL;
                default -> _default(tag);
            };
            case PLACE -> switch (tag.value()) {
                case "island" -> ISLAND;
                case "square", "archipelago" -> IGNORED;
                default -> _default(tag);
            };
            case HIGHWAY -> switch (tag.value()) {
                case "motorway" -> MOTORWAY;
                case "primary" -> PRIMARY;
                case "secondary" -> SECONDARY;
                case "tertiary" -> TERTIARY;
                case "service" -> SERVICE;
                case "rest_area" -> REST_AREA;
                case "track" -> TRACK;
                case "proposed" -> IGNORED;
                case "road", "residential", "unclassified", "raceway", "taxiway" -> ROAD;
                case "bridleway",
                        "construction",
                        "crossing",
                        "cycleway",
                        "footway",
                        "give_way",
                        "living_street",
                        "path",
                        "pedestrian",
                        "steps" -> PATH;
                default -> _default(tag);
            };
            default -> _default(tag);
        };
    }

    public enum Shape {
        POLYLINE,
        FILL
    }
}
