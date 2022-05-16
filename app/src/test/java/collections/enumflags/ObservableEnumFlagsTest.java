package collections.enumflags;

import collections.observable.Observer;
import drawing.Drawable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableEnumFlagsTest {
    ObservableEnumFlags<Drawable.Category> flags;

    @BeforeEach
    public void setUp() {
        flags = new ObservableEnumFlags<>();
    }

    @Test
    public void testToggle() {
        final boolean[] success = {false};

        flags.addObserver(event -> {
            if (!event.enabled() && event.variant() == Drawable.Category.CITY) success[0] = true;
        });

        flags.toggle(Drawable.Category.CITY);
        assertTrue(success[0]);
    }

    @Test
    public void testSet() {
        final boolean[] success = {false};

        flags.unset(Drawable.Category.CITY);
        
        flags.addObserver(event -> {
            if (event.enabled() && event.variant() == Drawable.Category.CITY) success[0] = true;
        });

        flags.set(Drawable.Category.CITY);
        assertTrue(success[0]);
    }

    @Test
    public void testUnset() {
        final boolean[] success = {false};

        flags.addObserver(event -> {
            if (!event.enabled() && event.variant() == Drawable.Category.CITY) success[0] = true;
        });

        flags.unset(Drawable.Category.CITY);
        assertTrue(success[0]);
    }
}
