package navigation;

import java.io.Serializable;

public record Road(String name, float distance, int maxSpeed)
        implements Comparable<Road>, Serializable {
    @Override
    public int compareTo(Road other) {
        return Float.compare(distance / maxSpeed, other.distance / other.maxSpeed);
    }
}
