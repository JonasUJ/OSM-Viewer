package collections.spatial;

import geometry.Point;
import geometry.Rect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearSearchTwoDTree<E> implements SpatialTree<E>, Serializable {
    private final int maxCount;
    private final Rect bounds;

    private Node<E> root;
    private int size;
    private int height;

    public LinearSearchTwoDTree(int maxCount) {
        this(maxCount, new Rect(0, 0, 1, 1));
    }

    public LinearSearchTwoDTree(int maxHeight, Rect bounds) {
        this.maxCount = maxHeight;
        this.bounds = bounds;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void insert(Point point, E value) {
        if (point == null) {
            throw new IllegalArgumentException("Parameter 'point' cannot be null.");
        }

        if (!bounds.contains(point)) {
            throw new IllegalArgumentException(
                    "The specified point is not contained within the bounds of the tree.");
        }

        if (isEmpty()) {
            root = new LeafNode<>(point, value);
            root.rect = bounds;

            size = 1;
            height = 1;
        } else {
            root = insert(point, value, root, 1);
        }
    }

    private Node<E> insert(Point point, E value, Node<E> node, int level) {
        if (node == null) {
            throw new IllegalArgumentException("The node parameter cannot be null.");
        }

        if (node.contains(point)) {
            // Just return if we insert an existing point.
            return node;
        }

        if (node instanceof LeafNode<E> leafNode) {
            if (leafNode.size() >= maxCount) {
                // The leaf node has reached the max count, so we need to split it into an ancestor node.
                var ancestorNode = splitLeafNode(point, value, leafNode, level);

                // Null means the ancestor node would have contained a child with no points (not valid).
                if (ancestorNode != null) {
                    size++;
                    height = Math.max(level + 1, height);

                    return ancestorNode;
                }
            }

            leafNode.add(point, value);
            size++;

            return node;
        }

        // The node is an ancestor node, so we traverse the tree normally.
        var ancestorNode = (AncestorNode<E>) node;
        var isLower = (level & 1) == 0 ? point.y() < ancestorNode.y() : point.x() < ancestorNode.x();

        if (isLower) {
            ancestorNode.left = insert(point, value, ancestorNode.left, level + 1);
        } else {
            ancestorNode.right = insert(point, value, ancestorNode.right, level + 1);
        }

        return node;
    }

    private AncestorNode<E> splitLeafNode(Point point, E value, LeafNode<E> node, int level) {
        // We split the list into two parts, the left and right having "smaller" and "greater" points
        // respectively.
        // This allows the new point to be a parent node for the two leaf nodes.
        var leftPoints = new ArrayList<Point>();
        var leftValues = new ArrayList<E>();
        var rightPoints = new ArrayList<Point>();
        var rightValues = new ArrayList<E>();

        for (int i = 0; i < node.size(); i++) {
            var nodePoint = node.points.get(i);
            var nodeValue = node.values.get(i);

            var isLower = (level & 1) == 0 ? nodePoint.y() < point.y() : nodePoint.x() < point.x();

            if (isLower) {
                leftPoints.add(nodePoint);
                leftValues.add(nodeValue);
            } else {
                rightPoints.add(nodePoint);
                rightValues.add(nodeValue);
            }
        }

        if (leftPoints.isEmpty() || rightPoints.isEmpty()) {
            // We would have created a child node with no points, which is not supported, so we wait for a
            // new point.
            return null;
        }

        var ancestorNode = new AncestorNode<>(point, value, node.rect);
        ancestorNode.left =
                new LeafNode<>(
                        leftPoints,
                        leftValues,
                        new Rect(
                                node.rect.top(),
                                node.rect.left(),
                                (level & 1) == 0 ? point.y() : node.rect.bottom(),
                                (level & 1) != 0 ? point.x() : node.rect.right()));
        ancestorNode.right =
                new LeafNode<>(
                        rightPoints,
                        rightValues,
                        new Rect(
                                (level & 1) == 0 ? point.y() : node.rect.top(),
                                (level & 1) != 0 ? point.x() : node.rect.left(),
                                node.rect.bottom(),
                                node.rect.right()));

        return ancestorNode;
    }

    @Override
    public boolean contains(Point point) {
        return contains(point, root, 1);
    }

    private boolean contains(Point point, Node<E> node, int level) {
        if (node == null) {
            return false;
        }

        if (node.contains(point)) {
            return true;
        }

        if (node instanceof LeafNode<E>) {
            // The above 'contains' call did not return true, so the point does not exist.
            return false;
        }

        var ancestorNode = (AncestorNode<E>) node;
        var isLower = (level & 1) == 0 ? point.y() < ancestorNode.y() : point.x() < ancestorNode.x();

        if (isLower) {
            return contains(point, ancestorNode.left, level + 1);
        } else {
            return contains(point, ancestorNode.right, level + 1);
        }
    }

    @Override
    public QueryResult<E> nearest(Point query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        if (isEmpty()) {
            return null;
        }

        var best = root.distanceSquaredTo(query);
        var champ = new QueryResult<>(best.point, best.value);

        return nearest(query, root, champ, best, 1);
    }

    private QueryResult<E> nearest(
            Point query, Node<E> node, QueryResult<E> champ, DistanceResult<E> best, int level) {
        // Check if the distance from the query point to the nearest point of
        // the node rectangle is greater than the current best distance.
        if (node == null || best.distance < node.rect.distanceSquaredTo(query)) {
            return champ;
        }

        // Set an actual best distance when we recur.
        best =
                new DistanceResult<>(champ.point().distanceSquaredTo(query), champ.point(), champ.value());
        var dist = node.distanceSquaredTo(query);

        // Check if the distance from the query point to the traversed point is less than
        // the distance from the query point to the current champion point.
        if (dist.distance < best.distance) {
            best = dist;
            champ = new QueryResult<>(dist.point, dist.value);
        }

        if (node instanceof LeafNode<E>) {
            // We cannot traverse the tree further from a leaf node.
            return champ;
        }

        var ancestorNode = (AncestorNode<E>) node;
        var isLower = (level & 1) == 0 ? query.y() < ancestorNode.y() : query.x() < ancestorNode.x();

        if (isLower) {
            champ = nearest(query, ancestorNode.left, champ, best, level + 1);

            // Decide if we need to go down and check the other child.
            // Compare the distance from the query point to the nearest point of the ancestorNode
            // rectangle
            // against the distance from the query point to the current champion point.
            // If it is smaller, there could potentially be a point that is closer to the query point
            // than the current champion point.
            if (ancestorNode.right != null
                    && ancestorNode.right.rect.distanceSquaredTo(query)
                    < champ.point().distanceSquaredTo(query)) {
                champ = nearest(query, ancestorNode.right, champ, best, level + 1);
            }
        } else {
            champ = nearest(query, ancestorNode.right, champ, best, level + 1);

            if (ancestorNode.left != null
                    && ancestorNode.left.rect.distanceSquaredTo(query)
                    < champ.point().distanceSquaredTo(query)) {
                champ = nearest(query, ancestorNode.left, champ, best, level + 1);
            }
        }

        return champ;
    }

    @Override
    public Iterable<QueryResult<E>> range(Rect query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        var results = new ArrayList<QueryResult<E>>();
        range(query, root, results);

        return results;
    }

    private void range(Rect query, Node<E> node, List<QueryResult<E>> results) {
        if (node == null || !node.rect.intersects(query)) {
            return;
        }

        if (node instanceof LeafNode<E> leafNode) {
            // For leaf nodes we need to iterate through the point list, and call 'contains' for each.
            for (int i = 0; i < leafNode.points.size(); i++) {
                var point = leafNode.points.get(i);

                if (query.contains(point)) {
                    var result = new QueryResult<>(point, leafNode.values.get(i));
                    results.add(result);
                }
            }

            return;
        }

        var ancestorNode = (AncestorNode<E>) node;

        if (query.contains(ancestorNode.point)) {
            var result = new QueryResult<>(ancestorNode.point, ancestorNode.value);
            results.add(result);
        }

        range(query, ancestorNode.left, results);
        range(query, ancestorNode.right, results);
    }

    private abstract static class Node<E> implements Serializable {
        protected Rect rect;

        public Node(Rect rect) {
            this.rect = rect;
        }

        public abstract boolean contains(Point point);

        public abstract DistanceResult<E> distanceSquaredTo(Point query);
    }

    private static class AncestorNode<E> extends Node<E> {
        private final Point point;
        private final E value;
        private Node<E> left;
        private Node<E> right;

        public AncestorNode(Point point, E value, Rect rect) {
            super(rect);

            this.point = point;
            this.value = value;
        }

        public float x() {
            return point.x();
        }

        public float y() {
            return point.y();
        }

        @Override
        public boolean contains(Point point) {
            return this.point.equals(point);
        }

        @Override
        public DistanceResult<E> distanceSquaredTo(Point query) {
            var distance = point.distanceSquaredTo(query);

            return new DistanceResult<>(distance, point, value);
        }
    }

    private static class LeafNode<E> extends Node<E> {
        private final List<Point> points;
        private final List<E> values;

        public LeafNode(Point firstPoint, E firstValue) {
            super(null);

            points = new ArrayList<>();
            values = new ArrayList<>();

            add(firstPoint, firstValue);
        }

        public LeafNode(List<Point> points, List<E> values, Rect rect) {
            super(rect);

            this.points = points;
            this.values = values;
        }

        public int size() {
            return points.size();
        }

        public void add(Point point, E value) {
            points.add(point);
            values.add(value);
        }

        @Override
        public boolean contains(Point point) {
            return points.contains(point);
        }

        @Override
        public DistanceResult<E> distanceSquaredTo(Point query) {
            var shortestDistance = Float.MAX_VALUE;
            var bestIndex = -1;

            for (int i = 0; i < points.size(); i++) {
                var point = points.get(i);
                var distance = point.distanceSquaredTo(query);

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestIndex = i;
                }
            }

            // 'bestIndex' should not be -1 here, as there should always be at least one point in the
            // list.
            assert bestIndex != -1;

            var point = points.get(bestIndex);
            var value = values.get(bestIndex);

            return new DistanceResult<>(shortestDistance, point, value);
        }
    }

    private record DistanceResult<E>(float distance, Point point, E value) {
    }
}
