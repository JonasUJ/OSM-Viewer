package collections.grid;

import geometry.Point;
import geometry.Rect;
import java.util.function.Function;

public class Grid<E> {
    final E[][] grid;
    private final Rect bounds;
    private final int cellSize;

    @SuppressWarnings("unchecked")
    public Grid(Rect bounds, int cellSize, Function<Point, E> init) {
        this.bounds = bounds;
        this.cellSize = cellSize;

        int width = ((int) bounds.width()) / cellSize + 1;
        int height = ((int) bounds.height()) / cellSize + 1;

        grid = (E[][]) new Object[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = init.apply(new Point(x, y));
            }
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
        return new QueryResult<>(
                this, toX(query.left()), toY(query.top()), toX(query.right()), toY(query.bottom()));
    }

    private int toX(double x) {
        return (int) Math.floor((x - bounds.left()) / cellSize);
    }

    private int toY(double y) {
        return (int) Math.floor((y - bounds.top()) / cellSize);
    }

    E gridGet(int x, int y) {
        if (x < 0 || y < 0 || y >= grid.length || x >= grid[0].length) return null;

        return grid[y][x];
    }
}
