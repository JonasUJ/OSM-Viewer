package collections.spatial;

import canvas.Renderer;
import drawing.Drawable;
import drawing.Drawing;
import geometry.Point;
import geometry.Rect;
import geometry.Vector2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TwoDTree<E> implements SpatialTree<E>, Serializable {
    private final Rect bounds;

    private Node<E> root;
    private int size;
    private int height;

    public TwoDTree() {
        this(new Rect(0, 0, 1, 1));
    }

    public TwoDTree(Rect bounds) {
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
            throw new IllegalArgumentException("The specified point is not contained within the bounds of the tree.");
        }

        if (isEmpty()) {
            root = insert(point, value, root, 0);
            root.rect = bounds;
            height = 1;
        } else {
            root = insert(point, value, root, 1);
        }
    }

    private Node<E> insert(Point point, E value, Node<E> node, int level) {
        if (node == null) {
            // The base case, insert a new node by returning it to the parent.
            size++;
            height = Math.max(level, height);

            return new Node<>(point, value);
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

    public void draw(Renderer renderer) {
        var drawing = new Drawing();
        addToDrawing(root, 1, drawing);
        renderer.draw(drawing);
    }

    private void addToDrawing(Node<E> node, int level, Drawing drawing) {
        if (node == null) {
            return;
        }

        drawing.draw(Drawing.create(new Vector2D(node.x(), node.y()), Drawable.POINT));

        var x1 = node.rect.left();
        var x2 = node.rect.right();
        var y1 = node.rect.bottom();
        var y2 = node.rect.top();

        Drawable drawable;

        if ((level & 1) == 0) {
            y1 = y2 = node.y();
            drawable = Drawable.PARTITION_HORIZONTAL;

        } else {
            x1 = x2 = node.x();
            drawable = Drawable.PARTITION_VERTICAL;
        }

        var point1 = new Vector2D(x1, y1);
        var point2 = new Vector2D(x2, y2);
        drawing.draw(Drawing.create(List.of(point1, point2), drawable));

        addToDrawing(node.left, level + 1, drawing);
        addToDrawing(node.right, level + 1, drawing);
    }

    private static class Node<E> implements Serializable {
        private final Point point;
        private final E value;
        private Rect rect;
        private Node<E> left;
        private Node<E> right;

        public Node(Point point, E value) {
            this.point = point;
            this.value = value;
        }

        public float x() {
            return point.x();
        }

        public float y() {
            return point.y();
        }
    }
}
