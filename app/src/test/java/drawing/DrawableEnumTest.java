package drawing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import osm.elements.OSMElement;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DrawableEnumTest {
    @Test
    public void testGetters() {
        // We can't test against the return values, as we might change them at some point.
        // All we need is for them not to throw.
        DrawableEnum.BASE.shape();
        DrawableEnum.BASE.color();
        DrawableEnum.BASE.size();
        DrawableEnum.BASE.category();
        DrawableEnum.BASE.detail();
    }

    @Test
    public void testFrom() {
        var elem = new OSMWay();
        elem.init(0);

        // Sample some cases

        elem.tags().clear();
        elem.tags().add(OSMTag.from("landuse", "forest"));
        assertEquals(DrawableEnum.FOREST, DrawableEnum.from(elem));

        elem.tags().clear();
        elem.tags().add(OSMTag.from("natural", "wood"));
        assertEquals(DrawableEnum.FOREST, DrawableEnum.from(elem));

        elem.tags().clear();
        elem.tags().add(OSMTag.from("amenity", "shelter"));
        assertEquals(DrawableEnum.BUILDING, DrawableEnum.from(elem));

        elem.tags().clear();
        elem.tags().add(OSMTag.from("leisure", "park"));
        assertEquals(DrawableEnum.PARK, DrawableEnum.from(elem));

        elem.tags().clear();
        elem.tags().add(OSMTag.from("building", "yes"));
        assertEquals(DrawableEnum.BUILDING, DrawableEnum.from(elem));

        elem.tags().clear();
        elem.tags().add(OSMTag.from("tourism", "museum"));
        assertEquals(DrawableEnum.MUSEUM, DrawableEnum.from(elem));
    }
}
