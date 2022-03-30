package collections.spacial;

import geometry.Point;
import geometry.Rect;
import java.util.ArrayList;
import java.util.List;

public class TwoDTree<E> implements SpacialTree<E> {
    private final float left, top, right, bottom;

    private Node<E> root;
    private int size;

    public TwoDTree() {
        this(0, 0, 1, 1);
    }

    public TwoDTree(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void insert(Point point, E value) {
        if (point == null) {
            throw new IllegalArgumentException("Parameter 'point' cannot be null.");
        }

        if (isEmpty()) {
            root = insert(point, value, root, 0);
            root.rect = new Rect(top, left, bottom, right);
        } else {
            root = insert(point, value, root, 1);
        }
    }

    private Node<E> insert(Point point, E value, Node<E> node, int level) {
        if (node == null) {
            // The base case, insert a new node by returning it to the parent.
            size++;

            return new Node<>(point, value, null);
        }

        if (node.point.equals(point)) {
            // Just return if we insert an existing point.
            return node;
        }

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to call insert on the left side if the new point is smaller at the y-axis.
            if (point.y() < node.y()) {
                // Traverse down the tree. If this is a null child an insert will be performed.
                node.left = insert(point, value, node.left, level + 1);

                // If the child node has an uninitialized rect we initialize it.
                if (node.left.rect == null) {
                    node.left.rect = new Rect(node.rect.top(), node.rect.left(), node.y(), node.rect.right());
                }
            } else {
                node.right = insert(point, value, node.right, level + 1);

                if (node.right.rect == null) {
                    node.right.rect =
                            new Rect(node.y(), node.rect.left(), node.rect.bottom(), node.rect.right());
                }
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to call insert on the left side if the new point is smaller at the x-axis.
            if (point.x() < node.x()) {
                node.left = insert(point, value, node.left, level + 1);

                if (node.left.rect == null) {
                    node.left.rect =
                            new Rect(node.rect.top(), node.rect.left(), node.rect.bottom(), node.x());
                }
            } else {
                node.right = insert(point, value, node.right, level + 1);

                if (node.right.rect == null) {
                    node.right.rect =
                            new Rect(node.rect.top(), node.x(), node.rect.bottom(), node.rect.right());
                }
            }
        }

        return node;
    }

    @Override
    public boolean contains(Point point) {
        return contains(point, root, 1);
    }

    private boolean contains(Point point, Node<E> node, int level) {
        if (node == null) {
            return false;
        }

        if (node.point.equals(point)) {
            return true;
        }

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to check the left side if the point is smaller at the y-axis.
            if (point.y() < node.y()) {
                return contains(point, node.left, level + 1);
            } else {
                return contains(point, node.right, level + 1);
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to check the left side if the point is smaller at the x-axis.
            if (point.x() < node.x()) {
                return contains(point, node.left, level + 1);
            } else {
                return contains(point, node.right, level + 1);
            }
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

        var best = root.point.distanceSquaredTo(query);
        var champ = new QueryResult<>(root.point, root.value);

        return nearest(query, root, champ, best, 1);
    }

    private QueryResult<E> nearest(
            Point query, Node<E> node, QueryResult<E> champ, float best, int level) {
        // Check if the distance from the query point to the nearest point of
        // the node rectangle is greater than the current best distance.
        if (node == null || best < node.rect.distanceSquaredTo(query)) {
            return champ;
        }

        // Set an actual best distance when we recur.
        best = champ.point().distanceSquaredTo(query);
        var dist = node.point.distanceSquaredTo(query);

        // Check if the distance from the query point to the traversed point is less than
        // the distance from the query point to the current champion point.
        if (dist < best) {
            best = dist;
            champ = new QueryResult<>(node.point, node.value);
        }

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to check the right side if the query point is greater at the y-axis.
            if (node.y() < query.y()) {
                // Traverse down the tree to check child nodes.
                champ = nearest(query, node.right, champ, best, level + 1);

                // Decide if we need to go down and check the other child.
                // Compare the distance from the query point to the nearest point of the node rectangle
                // against the distance from the query point to the current champion point.
                // If it is smaller, there could potentially be a point that is closer to the query point
                // than the current champion point.
                if (node.left != null
                        && node.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, node.left, champ, best, level + 1);

                if (node.right != null
                        && node.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.right, champ, best, level + 1);
                }
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to check the right side if the query point is greater at the x-axis.
            if (node.x() < query.x()) {
                champ = nearest(query, node.right, champ, best, level + 1);

                if (node.left != null
                        && node.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, node.left, champ, best, level + 1);

                if (node.right != null
                        && node.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.right, champ, best, level + 1);
                }
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

        if (query.contains(node.point)) {
            var result = new QueryResult<>(node.point, node.value);
            results.add(result);
        }

        range(query, node.left, results);
        range(query, node.right, results);
    }

    private static class Node<E> {
        private final Point point;
        private final E value;
        private Rect rect;
        private Node<E> left;
        private Node<E> right;

        public Node(Point point, E value, Rect rect) {
            this.point = point;
            this.value = value;
            this.rect = rect;
        }

        public float x() {
            return point.x();
        }

        public float y() {
            return point.y();
        }
    }
}
