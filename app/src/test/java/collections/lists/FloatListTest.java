package collections.lists;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatListTest {
    FloatList list;

    @BeforeEach
    public void setUp() {
        list = new FloatList(new float[] {0, 1, 2, 3, 4, 5, 6, 7});
    }

    @Test
    public void testAdd() {
        assertEquals(8, list.size());
        list.add((float) 8);
        assertEquals(8, list.get(8));
        assertEquals(9, list.size());
        list.add((float) 9);
    }

    @Test
    public void testSet() {
        assertEquals(8, list.size());
        list.set(4, (float) 8);
        assertEquals(8, list.get(4));
        assertEquals(8, list.size());
    }

    @Test
    public void testToArray() {
        list.add((float) 8);
        var arr = list.toArray();
        assertArrayEquals(new float[] {0,1,2,3,4,5,6,7,8}, arr);
    }

    @Test
    public void testGetArray() {
        list.add((float) 8);
        var arr = list.getArray();
        assertArrayEquals(new float[] {0,1,2,3,4,5,6,7,8,0,0,0,0,0,0,0}, arr);
    }

    @Test
    public void testTruncate() {
        list.truncate(3);
        var arr = list.toArray();
        assertArrayEquals(new float[] {0,1,2,3,4}, arr);
        assertEquals(5, list.size());
        list.truncate(8);
        assertEquals(0, list.size());
    }

    @Test
    public void testLimit() {
        list.limit(3);
        var arr = list.toArray();
        assertArrayEquals(new float[] {0,1,2}, arr);
        assertEquals(3, list.size());
        list.limit(0);
        assertEquals(0, list.size());
    }

    @Test
    public void testSwap() {
        assertEquals(0, list.get(0));
        assertEquals(7, list.get(7));
        list.swap(0, 7);
        assertEquals(7, list.get(0));
        assertEquals(0, list.get(7));
    }

    @Test
    public void testExtend() {
        var other = new FloatList(new float[] {8,9});
        list.extend(other);
        assertEquals(10, list.size());
        var arr = list.toArray();
        assertArrayEquals(new float[] {0,1,2,3,4,5,6,7,8,9}, arr);
    }
}
