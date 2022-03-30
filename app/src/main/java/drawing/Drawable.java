package drawing;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javafx.scene.paint.Color;
import osm.elements.OSMElement;
import osm.elements.OSMTag;

// TODO: Missing Area
public enum Drawable {
    BASE(Shape.FILL, Color.web("#f2efe9"), 0, Category.COAST),
    BEACH(Shape.FILL, Color.web("#f0e1ae"), 0, Category.LAND),
    SAND(Shape.FILL, Color.web("#f5e9c6"), 0, Category.LAND),
    FARMLAND(Shape.FILL, Color.web("#eef0d5"), 0, Category.LAND),
    FARMYARD(Shape.FILL, Color.web("#f5dcba"), 0, Category.LAND),
    FOREST(Shape.FILL, Color.web("#add19e"), 0, Category.LAND),
    RESIDENTIAL(Shape.FILL, Color.web("#e0dfdf"), 0, Category.CITY),
    PARK(Shape.FILL, Color.web("#c8facc"), 0, Category.LAND),
    CONSTRUCTION(Shape.FILL, Color.web("#c7c7b4"), 0, Category.CITY),
    CAMP_SITE(Shape.FILL, Color.web("#def6c0"), 0, Category.CITY),
    BREAK_WATER(Shape.FILL, Color.web("#aaaaaa"), 0, Category.LAND),
    PARKING(Shape.FILL, Color.web("#eeeeee"), 0, Category.CITY),
    ALLOTMENTS(Shape.FILL, Color.web("#c9e1bf"), 0, Category.CITY),
    LEISURE_PARK(Shape.FILL, Color.web("#dffce2"), 0, Category.LAND),
    GRASS(Shape.FILL, Color.web("#cdebb0"), 0, Category.LAND),
    GOLF(Shape.FILL, Color.web("#def6c0"), 0, Category.LAND),
    SPORTS_CENTRE(Shape.FILL, Color.web("#dffce2"), 0, Category.CITY),
    AMENITY_AREA(Shape.FILL, Color.web("#ffffe5"), 0, Category.CITY),
    HEATH(Shape.FILL, Color.web("#d6d99f"), 0, Category.LAND),
    BARE_ROCK(Shape.FILL, Color.web("#dfd9d3"), 0, Category.LAND),
    QUARRY(Shape.FILL, Color.web("#c9c7c7"), 0, Category.LAND),
    SCRUB(Shape.FILL, Color.web("#c8d7ab"), 0, Category.LAND),
    ORCHARD(Shape.FILL, Color.web("#aedfa3"), 0, Category.LAND),
    INDUSTRIAL(Shape.FILL, Color.web("#ebdbe8"), 0, Category.CITY),
    PLANT_NURSERY(Shape.FILL, Color.web("#aedfa3"), 0, Category.CITY),
    WATER(Shape.FILL, Color.web("#aad3df"), 0, Category.LAND),
    MUSEUM(Shape.FILL, Color.web("#f2efe9"), 0, Category.CITY),
    BUILDING(Shape.FILL, Color.web("#d9d0c9"), 0, Category.CITY),
    REST_AREA(Shape.FILL, Color.web("#efc8c8"), 0, Category.CITY),
    DWELLING(Shape.FILL, Color.web("#f2efe9"), 0, Category.CITY),
    PITCH(Shape.FILL, Color.web("#aae0cb"), 0, Category.CITY),
    RETAIL(Shape.FILL, Color.web("#ffd0c6"), 0, Category.CITY),
    PRISON(Shape.FILL, Color.web("#bdbdbd"), 0, Category.CITY),
    CLIFF(Shape.POLYLINE, Color.web("#9a9b99"), 0.1, Category.LAND),
    HEDGE(Shape.POLYLINE, Color.web("#add19e"), 0.05, Category.LAND),
    WALL(Shape.POLYLINE, Color.web("#bfbcb8"), 0.05, Category.CITY),
    PIER(Shape.POLYLINE, Color.web("#ffffff"), 0.05, Category.CITY),
    PATH(Shape.POLYLINE, Color.web("#edb39f"), 0.1, Category.ROAD),
    TRACK(Shape.POLYLINE, Color.web("#95dcc0"), 0.2, Category.ROAD),
    SERVICE(Shape.POLYLINE, Color.web("#ffffff"), 0.2, Category.ROAD),
    ROAD(Shape.POLYLINE, Color.web("#ffffff"), 0.4, Category.ROAD),
    TREE_ROW(Shape.POLYLINE, Color.web("#aacea3"), 0.2, Category.LAND),
    TERTIARY(Shape.POLYLINE, Color.web("#ffffff"), 0.7, Category.ROAD),
    SECONDARY(Shape.POLYLINE, Color.web("#f7fabf"), 0.8, Category.ROAD),
    PRIMARY(Shape.POLYLINE, Color.web("#fcd6a4"), 0.9, Category.ROAD),
    MOTORWAY(Shape.POLYLINE, Color.web("#e892a2"), 1.0, Category.ROAD),
    NATURE_RESERVE(Shape.POLYLINE, Color.rgb(0, 180, 30, 0.2), 0.6, Category.MISC),
    MILITARY(Shape.FILL, Color.rgb(255, 0, 0, 0.2), 0, Category.MISC),
    BOUNDS(Shape.POLYLINE, Color.rgb(255, 0, 150, 0.5), 5.0, Category.MISC),
    UNKNOWN(Shape.FILL, Color.web("#000000"), 0, Category.DEBUG),
    IGNORED(Shape.FILL, Color.web("#000000"), 0, Category.DEBUG);

    public static final FloatBuffer COLOR_MAP;
    public static final IntBuffer MAP;

    static {
        var array = new float[values().length * 4];
        for (var value : values()) {
            array[value.ordinal() * 4 + 0] = (float) value.color.getRed();
            array[value.ordinal() * 4 + 1] = (float) value.color.getGreen();
            array[value.ordinal() * 4 + 2] = (float) value.color.getBlue();
            array[value.ordinal() * 4 + 3] = (float) value.color.getOpacity();
        }
        COLOR_MAP = FloatBuffer.wrap(array);

        MAP = IntBuffer.allocate(values().length * 2);
        for (var drawable : values()) {
            MAP.put(1 << drawable.category.ordinal());
            MAP.put(drawable.ordinal());
        }
    }

    public final Shape shape;
    public final Color color;
    public final double size;
    public final Category category;

    Drawable(Shape shape, Color color, double size, Category category) {
        this.shape = shape;
        this.color = color;
        this.size = size;
        this.category = category;
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
                case "military" -> MILITARY;
                case "greenfield" -> IGNORED;
                case "retail" -> RETAIL;
                case "isolated_dwelling" -> DWELLING;
                case "grass", "meadow" -> GRASS;
                case "tree", "forest", "wood", "green_field" -> FOREST;
                default -> _default(tag);
            };
            case NATURAL -> switch (tag.value()) {
                case "peninsula" -> BASE;
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
                case "island" -> BASE;
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
