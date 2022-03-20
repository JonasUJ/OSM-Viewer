package navigation;

import static osm.elements.OSMTag.Key.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

public class Dijkstra implements OSMObserver, Serializable {
    private final List<Road> roads = new ArrayList<>();

    @Override
    public void onWay(OSMWay way) {
        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        String name =
                tags.stream().filter(t -> t.key() == NAME).map(OSMTag::value).findFirst().orElse(null);

        int maxSpeed =
                tags.stream()
                        .filter(
                                t ->
                                        t.key() == MAXSPEED
                                                && !t.value().equals("signals")
                                                && !t.value().equals("none"))
                        .map(t -> Integer.parseInt(t.value()))
                        .findFirst()
                        .orElse(0);

        var road = new Road(name, calculateDistance(way), maxSpeed);
        roads.add(road);
    }

    private static float calculateDistance(OSMWay way) {
        var nodes = way.nodes();
        var firstNode = nodes[0];
        float totalDistance = 0;

        for (int i = 1; i < nodes.length; i++) {
            var secondNode = nodes[i];

            var x1 = firstNode.lat();
            var y1 = firstNode.lon();
            var x2 = secondNode.lat();
            var y2 = secondNode.lon();

            var distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            totalDistance += distance;

            firstNode = secondNode;
        }

        return totalDistance;
    }
}
