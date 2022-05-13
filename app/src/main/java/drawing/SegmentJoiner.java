package drawing;

import collections.Entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

public class SegmentJoiner<T extends Entity> extends ArrayList<Segment<T>> {
    public SegmentJoiner(Collection<? extends Segment<T>> c) {
        super(c);
    }

    public void join() {
        var deque = new ArrayDeque<>(this);
        clear();

        // Alright the upper bound on this is quite awful, but how many segments, all with worst case
        // data, are we realistically joining? Most relations will run in linear time because they are
        // already joined from the OSM file.
        while (!deque.isEmpty()) {
            var current = deque.remove();

            var joined = false;
            var iter = deque.iterator();
            while (iter.hasNext()) {
                var other = iter.next();
                if (current.join(other)) {
                    iter.remove();
                    joined = true;
                }
            }

            if (!joined || current.first().equals(current.last())) {
                add(current);
            } else {
                deque.add(current);
            }
        }
    }
}
