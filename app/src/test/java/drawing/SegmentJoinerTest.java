package drawing;

import collections.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SegmentJoinerTest {
    SegmentJoiner<Entity> joiner;
    Entity one = Entity.withId(1);
    Entity two = Entity.withId(2);
    Entity three = Entity.withId(3);
    Entity four = Entity.withId(4);

    @BeforeEach
    public void setUp() {
        joiner = new SegmentJoiner<>(List.of(
                new Segment<>(List.of(one, two)),
                new Segment<>(List.of(two, one)),
                new Segment<>(List.of(one, two)),
                new Segment<>(List.of(three, four))
        ));
    }

    @Test
    public void testJoin() {
        joiner.join();

        assertEquals(2, joiner.size());

        var first = joiner.get(0);
        assertIterableEquals(List.of(three, four), first);

        var second = joiner.get(1);
        assertIterableEquals(List.of(two, one, one, two, two, one), second);
    }
}
