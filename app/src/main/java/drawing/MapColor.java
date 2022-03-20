package drawing;

import java.nio.FloatBuffer;
import javafx.scene.paint.Color;

public enum MapColor {
    ISLAND(Color.web("#f2efe9")),
    BEACH(Color.web("#f0e1ae")),
    SAND(Color.web("#f5e9c6")),
    FARMLAND(Color.web("#eef0d5")),
    FARMYARD(Color.web("#f5dcba")),
    FOREST(Color.web("#add19e")),
    RESIDENTIAL(Color.web("#e0dfdf")),
    PARK(Color.web("#c8facc")),
    CONSTRUCTION(Color.web("#c7c7b4")),
    CAMP_SITE(Color.web("#def6c0")),
    BREAK_WATER(Color.web("#aaaaaa")),
    PARKING(Color.web("#eeeeee")),
    ALLOTMENTS(Color.web("#c9e1bf")),
    LEISURE_PARK(Color.web("#dffce2")),
    GRASS(Color.web("#cdebb0")),
    GOLF(Color.web("#def6c0")),
    SPORTS_CENTRE(Color.web("#dffce2")),
    AMENITY_AREA(Color.web("#ffffe5")),
    HEATH(Color.web("#d6d99f")),
    BARE_ROCK(Color.web("#dfd9d3")),
    QUARRY(Color.web("#c9c7c7")),
    SCRUB(Color.web("#c8d7ab")),
    ORCHARD(Color.web("#aedfa3")),
    INDUSTRIAL(Color.web("#ebdbe8")),
    PLANT_NURSERY(Color.web("#aedfa3")),
    WATER(Color.web("#aad3df")),
    MUSEUM(Color.web("#f2efe9")),
    BUILDING(Color.web("#d9d0c9")),
    REST_AREA(Color.web("#efc8c8")),
    DWELLING(Color.web("#f2efe9")),
    PITCH(Color.web("#aae0cb")),
    RETAIL(Color.web("#ffd0c6")),
    PRISON(Color.web("#bdbdbd")),
    CLIFF(Color.web("#9a9b99")),
    HEDGE(Color.web("#add19e")),
    WALL(Color.web("#bfbcb8")),
    PIER(Color.web("#ffffff")),
    PATH(Color.web("#edb39f")),
    TRACK(Color.web("#95dcc0")),
    SERVICE(Color.web("#ffffff")),
    ROAD(Color.web("#ffffff")),
    TREE_ROW(Color.web("#aacea3")),
    NATURE_RESERVE(Color.web("#b5d3ae")), // TODO: Translucent?
    TERTIARY(Color.web("#ffffff")),
    SECONDARY(Color.web("#f7fabf")),
    PRIMARY(Color.web("#fcd6a4")),
    MOTORWAY(Color.web("#e892a2")),
    UNKNOWN(Color.BLACK),
    IGNORED(Color.BLACK);

    public static final FloatBuffer COLOR_MAP;

    static {
        var array = new float[values().length * 4];
        for (var value : values()) {
            array[value.ordinal() * 4 + 0] = (float) value.color.getRed();
            array[value.ordinal() * 4 + 1] = (float) value.color.getGreen();
            array[value.ordinal() * 4 + 2] = (float) value.color.getBlue();
            array[value.ordinal() * 4 + 3] = (float) value.color.getOpacity();
        }
        COLOR_MAP = FloatBuffer.wrap(array);
    }

    public final Color color;

    MapColor(Color color) {
        this.color = color;
    }

    public byte colorIdx() {
        return (byte) ordinal();
    }
}
