package osm;

import javafx.scene.paint.Color;

public enum Drawable {
    Sand(Shape.Fill, Color.web("#f5e9c6"), 0),
    Farmland(Shape.Fill, Color.web("#eef0d5"), 0),
    Park(Shape.Fill, Color.web("#c8facc"), 0),
    Farmyard(Shape.Fill, Color.web("#f5dcba"), 0),
    Residential(Shape.Fill, Color.web("#e0dfdf"), 0),
    Forest(Shape.Fill, Color.web("#add19e"), 0),
    Construction(Shape.Fill, Color.web("#c7c7b4"), 0),
    Parking(Shape.Fill, Color.web("#eeeeee"), 0),
    Allotments(Shape.Fill, Color.web("#c9e1bf"), 0),
    LeisurePark(Shape.Fill, Color.web("#dffce2"), 0),
    Grass(Shape.Fill, Color.web("#cdebb0"), 0),
    Golf(Shape.Fill, Color.web("#def6c0"), 0),
    SportsCentre(Shape.Fill, Color.web("#dffce2"), 0),
    AmenityArea(Shape.Fill, Color.web("#ffffe5"), 0),
    Heath(Shape.Fill, Color.web("#d6d99f"), 0),
    BareRock(Shape.Fill, Color.web("#dfd9d3"), 0),
    Quarry(Shape.Fill, Color.web("#c9c7c7"), 0),
    Scrub(Shape.Fill, Color.web("#c8d7ab"), 0),
    Orchard(Shape.Fill, Color.web("#aedfa3"), 0),
    Industrial(Shape.Fill, Color.web("#ebdbe8"), 0),
    Water(Shape.Fill, Color.web("#aad3df"), 0),
    Building(Shape.Fill, Color.web("#d9d0c9"), 0),
    RestArea(Shape.Fill, Color.web("#efc8c8"), 0),
    Dwelling(Shape.Fill, Color.web("#f2efe9"), 0),
    Pitch(Shape.Fill, Color.web("#aae0cb"), 0),
    Retail(Shape.Fill, Color.web("#ffd0c6"), 0),
    Prison(Shape.Fill, Color.web("#bdbdbd"), 0),
    Area(Shape.Polyline, Color.web("#90aa86"), 0.05),
    Cliff(Shape.Polyline, Color.web("#9a9b99"), 0.1),
    Path(Shape.Polyline, Color.web("#edb39f"), 0.1),
    Track(Shape.Polyline, Color.web("#95dcc0"), 0.2),
    ServiceRoad(Shape.Polyline, Color.web("#ffffff"), 0.2),
    Road(Shape.Polyline, Color.web("#ffffff"), 0.4),
    TreeRow(Shape.Polyline, Color.web("#aacea3"), 0.2),
    Tertiary(Shape.Polyline, Color.web("#ffffff"), 0.7),
    Secondary(Shape.Polyline, Color.web("#f7fabf"), 0.8),
    Primary(Shape.Polyline, Color.web("#fcd6a4"), 0.9),
    Motorway(Shape.Polyline, Color.web("#e892a2"), 1.0),
    Unknown(Shape.Fill, Color.BLACK, 0),
    Ignored(Shape.Fill, Color.BLACK, 0);

    public static final float length = values().length;

    public final Shape shape;
    public final Color color;
    public final double size;

    Drawable(Shape shape, Color color, double size) {
        this.shape = shape;
        this.color = color;
        this.size = size;
    }

    public float layer() {
        return ordinal() / length;
    }

    static Drawable _default(String key, String value) {
        System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
        return Unknown;
    }

    public static Drawable fromTag(String key, String value) {
        var tag = Tag.from(key);
        if (tag == null) return Unknown;
        return switch (tag) {
            case landuse -> switch (value) {
                case "allotments" -> Allotments;
                case "basin" -> Water;
                case "cemetery", "park", "village_green", "vineyard" -> Park;
                case "residential", "commercial" -> Residential;
                case "industrial" -> Industrial;
                case "recreation_ground" -> LeisurePark;
                case "construction" -> Construction;
                case "farmland", "greenhouse_horticulture" -> Farmland;
                case "farmyard" -> Farmyard;
                case "orchard" -> Orchard;
                case "quarry" -> Quarry;
                case "military" -> Ignored;
                case "retail" -> Retail;
                case "isolated_dwelling" -> Dwelling;
                case "grass", "meadow" -> Grass;
                case "tree", "forest", "wood", "green_field" -> Forest;
                default -> _default(key, value);
            };
            case natural -> switch (value) {
                case "wood" -> Forest;
                case "water" -> Water;
                case "cliff" -> Cliff;
                case "scrub" -> Scrub;
                case "tree_row" -> TreeRow;
                case "grassland" -> Grass;
                case "bare_rock" -> BareRock;
                case "wetland", "heath" -> Heath;
                case "coastline", "sand", "beach" -> Sand;
                default -> _default(key, value);
            };
            case amenity -> switch (value) {
                case "grave_yard" -> Park;
                case "prison" -> Prison;
                case "recycling" -> Industrial;
                case "parking", "bicycle_parking" -> Parking;
                case "school", "kindergarten", "hospital" -> AmenityArea;
                default -> _default(key, value);
            };
            case leisure -> switch (value) {
                case "park" -> Park;
                case "track" -> Track;
                case "sports_centre" -> SportsCentre;
                case "playground" -> LeisurePark;
                case "pitch" -> Pitch;
                case "marina" -> Water;
                case "garden" -> Grass;
                case "miniature_golf", "golf_course" -> Golf;
                default -> _default(key, value);
            };
            case area -> Area;
            case building -> Building;
            case highway -> switch (value) {
                case "motorway" -> Motorway;
                case "primary" -> Primary;
                case "secondary" -> Secondary;
                case "tertiary" -> Tertiary;
                case "service" -> ServiceRoad;
                case "rest_area" -> RestArea;
                case "track" -> Track;
                case "road", "residential", "unclassified", "raceway", "taxiway" -> Road;
                case "bridleway",
                        "construction",
                        "crossing",
                        "cycleway",
                        "footway",
                        "give_way",
                        "living_street",
                        "path",
                        "pedestrian",
                        "steps" -> Path;
                default -> _default(key, value);
            };
        };
    }

    public enum Shape {
        Polyline,
        Fill
    }
}
