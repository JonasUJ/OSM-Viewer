package collections.spatial;

import geometry.Point;
import geometry.Rect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public interface TwoDTreeTest<Tree extends SpatialTree<String>> {
    // An existing point in the tree (see createPopulatedTree()).
    Point existingPoint = new Point(0.2f, 0.3f);
    // A point that is NOT in the tree (see createPopulatedTree()).
    Point nonExistentPoint = new Point(0.5f, 0.5f);
    // The point and value that should be nearest to 'nonExistentPoint' (see createPopulatedTree()).
    QueryResult<String> nearestToNonExistent = new QueryResult<>(new Point(0.5f, 0.4f), "bar");

    Tree createTree();

    private SpatialTree<String> createPopulatedTree() {
        var tree = createTree();

        tree.insert(new Point(0.7f, 0.2f), "foo");
        tree.insert(new Point(0.5f, 0.4f), "bar");
        tree.insert(new Point(0.2f, 0.3f), "baz");
        tree.insert(new Point(0.6f, 0.7f), "qux");
        tree.insert(new Point(0.1f, 0.6f), "quux");
        tree.insert(new Point(1, 1), "quuz");

        return tree;
    }

    @Test
    default void testInsertOne() {
        var tree = createTree();

        tree.insert(new Point(0, 0), "foo");

        assertFalse(tree.isEmpty());
    }

    @Test
    default void testInsertTwo() {
        var tree = createTree();

        tree.insert(new Point(0, 0), "foo");
        tree.insert(new Point(1, 1), "bar");

        assertEquals(2, tree.size());
    }

    @Test
    default void testInsertExisting() {
        var tree = createPopulatedTree();
        var beforeSize = tree.size();
        var beforeValue = tree.nearest(existingPoint).value();

        tree.insert(existingPoint, beforeValue + "corge");

        assertEquals(beforeSize, tree.size());
        assertEquals(beforeValue, tree.nearest(existingPoint).value());
    }

    @Test
    default void testOutOfBounds() {
        var tree = createPopulatedTree();

        var exception = assertThrows(IllegalArgumentException.class, () -> tree.insert(new Point(1.1f, 1), "corge"));

        assertEquals("The specified point is not contained within the bounds of the tree.", exception.getMessage());
    }

    @Test
    default void testContains() {
        var tree = createPopulatedTree();

        var isContained = tree.contains(existingPoint);

        assertTrue(isContained);
    }

    @Test
    default void testDoesNotContain() {
        var tree = createPopulatedTree();

        var isContained = tree.contains(nonExistentPoint);

        assertFalse(isContained);
    }

    @Test
    default void testNearest() {
        var tree = createPopulatedTree();

        var nearestResult = tree.nearest(nonExistentPoint);

        assertEquals(nearestToNonExistent, nearestResult);
    }

    @Test
    default void testRange() {
        var tree = createPopulatedTree();

        var rangeResult = tree.range(new Rect(0, 0, 1, 1));

        // The range should contain all points as the query area was equal to the bounds.
        assertEquals(tree.size(), rangeResult.spliterator().getExactSizeIfKnown());
    }

    @Test
    default void testRangeOutOfBounds() {
        var tree = createPopulatedTree();

        var rangeResult = tree.range(new Rect(1.1f, 1.1f, 2, 2));

        assertEquals(0, rangeResult.spliterator().getExactSizeIfKnown());
    }
}
