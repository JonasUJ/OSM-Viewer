package collections.grid;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class QueryResult<E> implements Iterable<E> {
    private final Grid<E> grid;
    private final int sx, sy, ex, ey;

    QueryResult(Grid<E> grid, int sx, int sy, int ex, int ey) {
        this.grid = grid;
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QueryResult qr && sx == qr.sx && sy == qr.sy && grid == qr.grid;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int x = sx, y = sy;

            @Override
            public boolean hasNext() {
                // 1 branch missed in codecov because Jacoco can't see the JVM short-circuiting :'(
                return x <= ex && y <= ey;
            }

            @Override
            public E next() {
                E e;

                do {
                    if (!hasNext()) throw new NoSuchElementException();

                    e = grid.gridGet(x, y);

                    x++;
                    if (x > ex) {
                        x = sx;
                        y++;
                    }
                } while (e == null);

                return e;
            }
        };
    }
}
