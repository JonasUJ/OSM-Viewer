package drawing;

import collections.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Segment<T extends Entity> extends ArrayList<T> {
    public Segment(Collection<? extends T> c) {
        super(c);
    }

    public T first() {
        return this.get(0);
    }

    public T last() {
        return this.get(size() - 1);
    }

    public void reverse() {
        Collections.reverse(this);
    }

    public boolean join(Segment<T> other) {
        if (other.last().equals(first())) {
            reverse();
            other.reverse();
        } else if (first().equals(other.first())) {
            reverse();
        } else if (last().equals(other.last())) {
            other.reverse();
        } else if (!last().equals(other.first())) {
            return false;
        }

        addAll(other);
        return true;
    }
}
