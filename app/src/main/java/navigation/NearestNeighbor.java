package navigation;

import collections.spatial.LinearSearchTwoDTree;
import collections.spatial.SpatialTree;
import geometry.Point;
import geometry.Rect;
import javafx.util.Pair;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static osm.elements.OSMTag.Key.HIGHWAY;
import static osm.elements.OSMTag.Key.NAME;

public class NearestNeighbor implements OSMObserver, Serializable {
    private transient Rect bounds;
    private transient List<Pair<Point, String>> nodeCache = new ArrayList<>();

    private SpatialTree<String> tree;

    @Override
    public void onBounds(Rect bounds) {
        this.bounds = bounds;
        tree = new LinearSearchTwoDTree<>(1000, bounds);
    }

    @Override
    public void onWay(OSMWay way) {
        assert tree != null;

        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        var name = tags.stream().filter(t -> t.key() == NAME).map(OSMTag::value).findAny().orElse(null);

        if (name == null) {
            return;
        }

        for (var node : way.nodes()) {
            var point = new Point((float) node.lon(), (float) node.lat());
            var pair = new Pair<>(point, name);
            nodeCache.add(pair);
        }
    }

    @Override
    public void onFinish() {
        var nodes = nodeCache;
        nodeCache = null;
        addToTree(nodes, 0);
    }

    public String nearestRoad(Point query) {
        var nearestResult = tree.nearest(query);

        return nearestResult.value();
    }

    private void addToTree(List<Pair<Point, String>> nodes, int level) {
        nodes.sort(
                (first, second) ->
                        (level & 1) == 0
                                ? Float.compare(first.getKey().x(), second.getKey().x())
                                : Float.compare(first.getKey().y(), second.getKey().y()));

        var halfSize = nodes.size() / 2;
        var median = nodes.get(halfSize);
        var point = median.getKey();
        var name = median.getValue();

        if (bounds.contains(point)) {
            tree.insert(point, name);
        }

        if (nodes.size() == 1) {
            return;
        }

        var firstHalf = nodes.subList(0, halfSize);
        var secondHalf = nodes.subList(halfSize, nodes.size());
        addToTree(firstHalf, level + 1);
        addToTree(secondHalf, level + 1);
    }
}
