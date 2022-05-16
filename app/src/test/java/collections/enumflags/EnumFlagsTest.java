package collections.enumflags;

import drawing.Drawable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumFlagsTest {
    EnumFlags<Drawable.Category> flags;

    @BeforeEach
    public void setUp() {
        flags = new EnumFlags<>();
    }

    @Test
    public void testCtor() {
        var other = new EnumFlags<Drawable.Category>(false);
        for (var variant : Drawable.Category.values()) {
            assertTrue(flags.isSet(variant));
            assertFalse(other.isSet(variant));
        }
    }

    @Test
    public void testToggle() {
        assertTrue(flags.isSet(Drawable.Category.CITY));
        flags.toggle(Drawable.Category.CITY);
        assertFalse(flags.isSet(Drawable.Category.CITY));
        flags.toggle(Drawable.Category.CITY);
        assertTrue(flags.isSet(Drawable.Category.CITY));
    }

    @Test
    public void testSet() {
        assertTrue(flags.isSet(Drawable.Category.CITY));
        flags.unset(Drawable.Category.CITY);
        assertFalse(flags.isSet(Drawable.Category.CITY));
        flags.unset(Drawable.Category.CITY);
        assertFalse(flags.isSet(Drawable.Category.CITY));
        flags.set(Drawable.Category.CITY);
        assertTrue(flags.isSet(Drawable.Category.CITY));
        flags.set(Drawable.Category.CITY);
        assertTrue(flags.isSet(Drawable.Category.CITY));
    }

    @Test
    public void getFlags() {
        var other = new EnumFlags<Drawable.Category>(false);
        for (var variant : Drawable.Category.values()) {
            other.set(variant);
        }
        assertEquals((1 << Drawable.Category.values().length) - 1, other.getFlags());
    }
}
