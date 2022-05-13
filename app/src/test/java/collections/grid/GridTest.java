package collections.grid;

import static org.junit.jupiter.api.Assertions.*;

import geometry.Point;
import geometry.Rect;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GridTest {
    private static final Rect bounds = new Rect(10, 10, 20, 40);
    private Grid<Point> grid;

    @BeforeEach
    public void setUp() {
        grid = new Grid<>(bounds, 8, p -> p);
    }

    @Test
    public void testGetPoint() {
        var p = grid.get(new Point(10, 10));

        assertEquals(new Point(0, 0), p);
    }

    @Test
    public void testGetXY() {
        var p = grid.get(10, 10);

        assertEquals(new Point(0, 0), p);
    }

    @Test
    public void testGetOutside() {
        // y == 24, which is outside the bounds, but the cellSize should be big enough that we hit a
        // cell anyway.
        var p = grid.get(30, 24);

        assertEquals(new Point(2, 1), p);
    }

    @Test
    public void testOutOfBoundsNull() {
        assertNull(grid.get(0, 0));
        assertNull(grid.get(10, 0));
        assertNull(grid.get(100, 10));
        assertNull(grid.get(100, 100));
    }

    @Test
    public void testRange() {
        var res = grid.range(new Rect(12, 12, 20, 20));
        var iter = res.iterator();

        assertEquals(new Point(0, 0), iter.next());
        assertEquals(new Point(1, 0), iter.next());
        assertEquals(new Point(0, 1), iter.next());
        assertEquals(new Point(1, 1), iter.next());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void testRangeOutOfBounds() {
        var res = grid.range(new Rect(8, 8, 30, 16));
        var iter = res.iterator();

        assertEquals(new Point(0, 0), iter.next());
        assertEquals(new Point(0, 1), iter.next());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void testRangeEquals() {
        var range1 = grid.range(new Rect(5, 5, 20, 20));
        var range2 = grid.range(new Rect(12, 12, 24, 24));
        var range3 = grid.range(new Rect(14, 14, 20, 20));

        assertNotEquals(range1, range2);
        assertNotEquals(range1, range3);
        assertEquals(range2, range3);
        assertNotEquals(range3, new Point(0, 0));
    }
}
