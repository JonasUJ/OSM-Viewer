package drawing;

import collections.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SegmentTest {
    Segment<Entity> segment1;
    Segment<Entity> segment2;
    Segment<Entity> segment3;
    Segment<Entity> segment4;
    Entity one = Entity.withId(1);
    Entity two = Entity.withId(2);
    Entity three = Entity.withId(3);
    Entity four = Entity.withId(4);

    @BeforeEach
    public void setUp() {
        segment1 = new Segment<>(List.of(one, two));
        segment2 = new Segment<>(List.of(two, one));
        segment3 = new Segment<>(List.of(one, two));
        segment4 = new Segment<>(List.of(three, four));
    }

    @Test
    public void testFirst() {
        assertEquals(one, segment1.first());
        assertEquals(two, segment2.first());
    }

    @Test
    public void testLast() {
        assertEquals(two, segment1.last());
        assertEquals(one, segment2.last());
    }

    @Test
    public void testReverse() {
        segment1.reverse();
        assertIterableEquals(segment2, segment1);
    }

    @Test
    public void testJoin() {
        assertTrue(segment1.join(segment3));
        assertIterableEquals(List.of(two, one, one, two), segment1);

        assertTrue(segment2.join(segment3));
        assertIterableEquals(List.of(one, two, two, one), segment2);

        assertFalse(segment1.join(segment2));

        assertTrue(segment3.join(segment2));
        assertIterableEquals(List.of(two, one, one, two, two, one), segment3);

        segment3.add(Entity.withId(3));
        assertTrue(segment3.join(segment4));
        assertIterableEquals(List.of(two, one, one, two, two, one, three, three, four), segment3);
    }
}
