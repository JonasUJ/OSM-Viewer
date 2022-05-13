package navigation;

import geometry.Point;

import java.io.Serializable;

public record Road(String name, Point from, Point to, RoadRole role) implements Serializable {
}
