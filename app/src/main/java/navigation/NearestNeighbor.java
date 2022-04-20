package navigation;

import static osm.elements.OSMTag.Key.HIGHWAY;
import static osm.elements.OSMTag.Key.NAME;

import collections.spatial.TwoDTree;
import geometry.Point;
import geometry.Rect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

public class NearestNeighbor implements OSMObserver, Serializable {
    private transient List<Pair<Point, String>> nodeCache = new ArrayList<>();
    private TwoDTree<String> twoDTree;

    @Override
    public void onBounds(Rect bounds) {
        twoDTree = new TwoDTree<>((float)Point.geoToMapX(bounds.left()),
            (float)Point.geoToMapY(bounds.bottom()),
            (float)Point.geoToMapX(bounds.right()),
            (float)Point.geoToMapY(bounds.top()));
    }

    @Override
    public void onWay(OSMWay way) {
        assert twoDTree != null;

        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        var name = tags.stream().filter(t -> t.key() == NAME).map(OSMTag::value).findAny().orElse(null);

        if (name == null) {
            return;
        }

        for (var node : way.nodes()) {
            var point = new Point((float)Point.geoToMapX(node.lon()), (float)Point.geoToMapY(node.lat()));
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

    public String nearestTo(Point query) {
        var nearestResult = twoDTree.nearest(query);

        return nearestResult.value();
    }

    private void addToTree(List<Pair<Point, String>> nodes, int level) {
        nodes.sort((first, second) -> (level & 1) == 0 ?
            Float.compare(first.getKey().x(), second.getKey().x()) :
            Float.compare(first.getKey().y(), second.getKey().y()));

        var halfSize = nodes.size() / 2;
        var median = nodes.get(halfSize);
        var point = median.getKey();
        var name = median.getValue();

        twoDTree.insert(point, name);

        if (nodes.size() == 1) {
            return;
        }

        var firstHalf = nodes.subList(0, halfSize);
        var secondHalf = nodes.subList(halfSize, nodes.size());
        addToTree(firstHalf, level + 1);
        addToTree(secondHalf, level + 1);
    }
}
