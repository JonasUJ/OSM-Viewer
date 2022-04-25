package collections.btree;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BTreeSetTest {
    private static final int iterations = (int) Math.pow(BTreeSet.M, 4);
    private BTreeSet<Integer> btree;

    @BeforeEach
    public void setUp() {
        btree = new BTreeSet<>();
    }

    @Test
    public void testAsc() {
        // Arrange
        var ints = new ArrayList<Integer>();

        // Act
        for (Integer i = 0; i < iterations; i++) {
            btree.add(i);
            ints.add(i);
        }

        // Assert
        for (var i : ints) {
            assertSame(i, btree.get(i));
        }
    }

    @Test
    public void testDesc() {
        // Arrange
        var ints = new ArrayList<Integer>();

        // Act
        for (Integer i = iterations; i > 0; i--) {
            btree.add(i);
            ints.add(i);
        }

        // Assert
        for (var i : ints) {
            assertSame(i, btree.get(i));
        }
    }

    @Test
    public void testRandom() {
        // Not really a very specific test, but should catch some problems.

        // Arrange
        var ints = new HashSet<Integer>();
        var random = new Random(BTreeSet.M);

        // Act
        for (int i = 0; i < iterations; i++) {
            Integer I = random.nextInt();
            btree.add(I);
            ints.add(I);
        }

        // Assert
        for (var i : ints) {
            assertEquals(i, btree.get(i));
        }
    }

    @Test
    public void testAddNullNPE() {
        assertThrows(NullPointerException.class, () -> btree.add(null));
    }

    @Test
    public void testGetNullNPE() {
        assertThrows(NullPointerException.class, () -> btree.get(null));
    }

    @Test
    public void testContainsNullNPE() {
        assertThrows(NullPointerException.class, () -> btree.contains(null));
    }

    @Test
    public void testOverwriteEqualKey() {
        // Arrange
        // Integers from -128 to unknown are cached and will return the same object. We bypass the cache
        // by using Integer.MIN_VALUE.
        var i = Integer.MIN_VALUE;
        var key1 = Integer.valueOf(i);
        var key2 = Integer.valueOf(i);

        // Act
        btree.add(key1);
        btree.add(key2);

        // Assert
        assertNotSame(key1, btree.get(i));
        assertSame(key2, btree.get(i));
    }

    @Test
    public void testAddSameAtSplit() {
        // Arrange
        Integer I = -1;
        btree.add(I);
        for (int i = 0; i < BTreeSet.M - 1; i++) {
            btree.add(i);
        }

        // Act
        btree.add(I);

        // Assert
        assertTrue(btree.root instanceof Leaf<Integer>);
    }

    @Test
    public void testContainsPresent() {
        btree.add(0);
        assertTrue(btree.contains(0));
    }

    @Test
    public void testContainsMissing() {
        btree.add(0);
        assertFalse(btree.contains(1));
    }

    @Test
    public void testLoitering() {
        // Arrange
        var ints = new ArrayList<WeakReference<Integer>>();

        // Act
        // Integers from -128 to unknown are cached and will return the same object. We bypass the cache
        // by using Integer.MIN_VALUE.
        for (Integer i = Integer.MIN_VALUE; i < Integer.MIN_VALUE + 2 * BTreeSet.M; i++) {
            btree.add(i);
            ints.add(new WeakReference<>(i));
        }

        // Overwrite all objects in the set
        for (var i : ints) {
            // Despite what IntelliJ claims, this unboxing is anything but unnecessary.
            btree.add(i.get().intValue());
        }

        // Does this make the test unreliable?
        System.gc();

        // Assert
        for (var i : ints) {
            assertNull(i.get());
        }
    }

    @Test
    public void testEntrySetKeyAssertion() {
        var entry = new Entry<>(Integer.MIN_VALUE, null);
        assertThrows(AssertionError.class, () -> entry.setKey(0));
    }

    @Test
    public void testStorageContainsDefault() {
        Storage<Integer> mock =
                new Storage<>() {
                    @Override
                    public boolean isFull() {
                        return false;
                    }

                    @Override
                    public Storage<Integer> split(Integer integer) {
                        return null;
                    }

                    @Override
                    public void insert(Integer integer) {}

                    @Override
                    public Integer lowest() {
                        return null;
                    }

                    @Override
                    public Integer get(Integer integer) {
                        return integer == 0 ? 0 : null;
                    }
                };

        assertTrue(mock.contains(0));
        assertFalse(mock.contains(1));
    }
}
