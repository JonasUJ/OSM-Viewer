package collections.lru;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LRUCacheTest {
    private LRUCache<String, Integer> cache;

    @BeforeEach
    public void setUp() {
        cache = new LRUCache<>(4);
    }

    @Test
    public void testFillHalf() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1

        // Assert
        assertEquals(2, cache.size());
        assertEquals(1, cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertNull(cache.get("three"));
        assertNull(cache.get("four"));
        assertNull(cache.get("five"));
    }

    @Test
    public void testUpdate() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("one", 10); // 10 2

        // Assert
        assertEquals(2, cache.size());
        assertEquals(10, cache.get("one"));
        assertEquals(2, cache.get("two"));
    }

    @Test
    public void testUpdateEvicted() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.set("four", 4); // 4 3 2 1
        cache.set("five", 5); // 5 4 3 2
        cache.set("one", 10); // 10 5 4 3

        // Assert
        assertEquals(4, cache.size());
        assertEquals(10, cache.get("one"));
        assertNull(cache.get("two"));
        assertEquals(3, cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testEvictLinear() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.set("four", 4); // 4 3 2 1
        cache.set("five", 5); // 5 4 3 2

        // Assert
        assertEquals(4, cache.size());
        assertNull(cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertEquals(3, cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testGetBack() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.set("four", 4); // 4 3 2 1
        cache.get("one"); // 1 4 3 2
        cache.set("five", 5); // 5 1 4 3

        // Assert
        assertEquals(4, cache.size());
        assertEquals(1, cache.get("one"));
        assertNull(cache.get("two"));
        assertEquals(3, cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testGetMultiple() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.get("one"); // 1 2
        cache.set("three", 3); // 3 1 2
        cache.get("two"); // 2 3 1
        cache.set("four", 4); // 4 2 3 1
        cache.get("three"); // 3 4 2 1
        cache.set("five", 5); // 5 3 4 2

        // Assert
        assertEquals(4, cache.size());
        assertNull(cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertEquals(3, cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testClear() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.clear(); //
        cache.set("four", 4); // 4
        cache.set("five", 5); // 5 4

        // Assert
        assertEquals(2, cache.size());
        assertNull(cache.get("one"));
        assertNull(cache.get("two"));
        assertNull(cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testGetCapacity() {
        assertEquals(4, cache.getCapacity());
    }

    @Test
    public void testDecreaseCapacity() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.set("four", 4); // 4 3 2 1
        cache.get("one"); // 1 4 3 2
        cache.setCapacity(2); // 1 4
        cache.set("five", 5); // 5 1

        // Assert
        assertEquals(2, cache.size());
        assertEquals(1, cache.get("one"));
        assertNull(cache.get("two"));
        assertNull(cache.get("three"));
        assertNull(cache.get("four"));
        assertEquals(5, cache.get("five"));
    }

    @Test
    public void testIncreaseCapacity() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.set("three", 3); // 3 2 1
        cache.set("four", 4); // 4 3 2 1
        cache.get("one"); // 1 4 3 2
        cache.setCapacity(6); // 1 4 3 2
        cache.set("five", 5); // 5 1 4 3 2
        cache.set("six", 6); // 6 5 1 4 3 2
        cache.set("seven", 7); // 7 6 5 1 4 3

        // Assert
        assertEquals(6, cache.size());
        assertEquals(1, cache.get("one"));
        assertNull(cache.get("two"));
        assertEquals(3, cache.get("three"));
        assertEquals(4, cache.get("four"));
        assertEquals(5, cache.get("five"));
        assertEquals(6, cache.get("six"));
        assertEquals(7, cache.get("seven"));
    }

    @Test
    public void testSetCapacity1() {
        // Act
        cache.set("one", 1); // 1
        cache.set("two", 2); // 2 1
        cache.setCapacity(1); // 2
        cache.set("three", 3); // 3

        // Assert
        assertEquals(1, cache.size());
        assertNull(cache.get("one"));
        assertNull(cache.get("two"));
        assertEquals(3, cache.get("three"));
    }

    @Test
    public void testSetCapacityIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> cache.setCapacity(0));
    }

    @Test
    public void testDefaultCtor() {
        var cache = new LRUCache<>();
        assertTrue(cache.getCapacity() > 0);
    }

    @Test
    public void testEvictionAware() {
        var cache = new LRUCache<String, Object>(1);

        var wasEvicted = new AtomicBoolean(false);

        cache.set(
                "test",
                (EvictionAware)
                        () -> {
                            assertNull(cache.get("test"));
                            assertEquals("other", cache.get("other"));
                            wasEvicted.set(true);
                        });
        cache.set("other", "other");

        assertTrue(wasEvicted.get());
    }
}
