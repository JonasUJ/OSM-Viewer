package collections.grid;

import geometry.Point;
import geometry.Rect;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class Grid<E> implements Iterable<E> {
    public final float cellSize;
    final E[][] grid;
    private final Point topLeft;

    @SuppressWarnings("unchecked")
    public Grid(Rect bounds, float cellSize, Function<Point, E> init) {
        this.topLeft = bounds.getTopLeft();
        this.cellSize = cellSize;

        int width = toX(bounds.right()) + 1;
        int height = toY(bounds.bottom()) + 1;

        grid = (E[][]) new Object[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = init.apply(new Point(x, y));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Grid(Rect bounds, float cellSize, Map<Point, E> map) {
        this.topLeft = bounds.getTopLeft();
        this.cellSize = cellSize;

        int width = toX(bounds.right()) + 1;
        int height = toY(bounds.bottom()) + 1;

        grid = (E[][]) new Object[height][width];

        for (var entry : map.entrySet()) {
            var p = entry.getKey();
            grid[(int) p.y()][(int) p.x()] = entry.getValue();
        }
    }

    public E get(Point point) {
        return get(point.x(), point.y());
    }

    public E get(double x, double y) {
        var tx = toX(x);
        var ty = toY(y);

        return gridGet(tx, ty);
    }

    public QueryResult<E> range(Rect query) {
        return range(query.top(), query.left(), query.bottom(), query.right());
    }

    public QueryResult<E> range(double top, double left, double bottom, double right) {
        return new QueryResult<>(this, toX(left), toY(top), toX(right), toY(bottom));
    }

    public int size() {
        return grid.length * grid[0].length;
    }

    private int toX(double x) {
        return (int) Math.floor((x - topLeft.x()) / cellSize);
    }

    private int toY(double y) {
        return (int) Math.floor((y - topLeft.y()) / cellSize);
    }

    E gridGet(int x, int y) {
        if (x < 0 || y < 0 || y >= grid.length || x >= grid[0].length) return null;

        return grid[y][x];
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int i = 0;
            private int j = 0;

            @Override
            public boolean hasNext() {
                return grid.length > 0 && j < grid[i].length;
            }

            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();

                E e = grid[i++][j];

                if (i == grid.length) {
                    i = 0;
                    j++;
                }

                return e;
            }
        };
    }
}
