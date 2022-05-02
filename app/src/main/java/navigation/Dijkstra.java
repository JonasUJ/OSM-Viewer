package navigation;

import static osm.elements.OSMTag.Key.*;

import java.io.Serializable;
import java.util.*;

import collections.enumflags.EnumFlags;
import geometry.Point;
import osm.OSMObserver;
import osm.elements.*;

public class Dijkstra implements OSMObserver, Serializable {
    private final Graph graph;
    private final Map<Long, Float> distTo;
    private final Map<Long, Edge> edgeTo;
    private final Set<Long> settled;
    private final PriorityQueue<Node> queue;

    private EdgeRole mode;

    public Dijkstra() {
        graph = new Graph();
        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
        settled = new HashSet<>();
        queue = new PriorityQueue<>();

        mode = EdgeRole.CAR;
    }

    @Override
    public void onWay(OSMWay way) {
        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        var edgeRoles = getEdgeRoles(way);

        if (edgeRoles.getFlags() == 0) {
            // The way is a highway that should not be used for navigation.
            return;
        }

        int maxSpeed = tags.stream()
            .filter(t -> t.key() == MAXSPEED &&
                !t.value().equals("signals") &&
                !t.value().equals("none"))
            .map(t -> Integer.parseInt(t.value()))
            .findFirst()
            .orElse(getExpectedMaxSpeed(way));

        var direction = determineDirection(way);

        if (direction == Direction.UNKNOWN) {
            direction = Direction.BOTH;
        }

        var nodes = way.nodes();
        var firstNode = nodes[0];

        for (int i = 1; i < nodes.length; i++) {
            var secondNode = nodes[i];

            var firstVertex = coordinatesToLong((float)firstNode.lon(), (float)firstNode.lat());
            var secondVertex = coordinatesToLong((float)secondNode.lon(), (float)secondNode.lat());
            var distance = calculateDistance(firstNode, secondNode);

            if (direction == Direction.SINGLE || direction == Direction.BOTH) {
                var edge = new Edge(firstVertex, secondVertex, distance, maxSpeed, edgeRoles);
                graph.addEdge(edge);
            }

            if (direction == Direction.REVERSE || direction == Direction.BOTH) {
                var edge = new Edge(secondVertex, firstVertex, distance, maxSpeed, edgeRoles);
                graph.addEdge(edge);
            }

            firstNode = secondNode;
        }
    }

    private static long coordinatesToLong(float lon, float lat) {
        var lonBits = Float.floatToIntBits(lon);
        var latBits = Float.floatToIntBits(lat);

        return (((long)lonBits) << 32) | (latBits & 0xFFFFFFFFL);
    }

    private static Point longToCoordinates(long value) {
        var lonBits = (int)(value >>> 32);
        var latBits = (int)(value & 0xFFFFFFFFL);

        var lon = Float.intBitsToFloat(lonBits);
        var lat = Float.intBitsToFloat(latBits);

        return new Point(lon, lat);
    }

    public List<Point> shortestPath(Point from, Point to, EdgeRole mode) {
        this.mode = mode;

        distTo.clear();
        edgeTo.clear();
        settled.clear();
        queue.clear();

        var sourceVertex = Dijkstra.coordinatesToLong(from.x(), from.y());
        var targetVertex = Dijkstra.coordinatesToLong(to.x(), to.y());

        queue.add(new Node(sourceVertex, 0));
        distTo.put(sourceVertex, 0f);

        while (!queue.isEmpty()) {
            var vertex = queue.remove().vertex;

            if (vertex == targetVertex) {
                // We've reached the target destination, no need to continue.
                break;
            }

            if (settled.contains(vertex)) {
                continue;
            }

            // We settle the vertex BEFORE relaxing (and not after), in case there happens to be a loop.
            settled.add(vertex);
            relax(vertex, targetVertex);
        }

        var vertices = extractPath(sourceVertex, targetVertex, edgeTo);

        if (vertices == null) {
            return null;
        }

        return vertices.stream().map(Dijkstra::longToCoordinates).toList();
    }

    private void relax(long vertex, long target) {
        for (var edge : graph.adjacent(vertex)) {
            if (!edge.hasRole(mode)) {
                continue;
            }

            var to = edge.to();

            if (!settled.contains(to)) {
                var newDistance = distTo.get(vertex) + calculateWeight(edge);

                if (newDistance < distTo.computeIfAbsent(to, v -> Float.POSITIVE_INFINITY)) {
                    distTo.put(to, newDistance);
                    edgeTo.put(to, edge);
                }

                var priority = distTo.get(to) + heuristic(to, target);
                queue.add(new Node(to, priority));
            }
        }
    }

    private float calculateWeight(Edge edge) {
        float weight = edge.distance();

        if (mode == EdgeRole.CAR) {
            weight /= edge.maxSpeed();
        }

        return weight;
    }

    private float heuristic(long from, long to) {
        var fromCoordinates = longToCoordinates(from);
        var toCoordinates = longToCoordinates(to);

        var x1 = fromCoordinates.x();
        var y1 = fromCoordinates.y();
        var x2 = toCoordinates.x();
        var y2 = toCoordinates.y();

        var heuristic = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        if (mode == EdgeRole.CAR) {
            // Divide by the highest possible speed on a highway.
            heuristic /= 130;
        }

        return (float)heuristic;
    }

    private static List<Long> extractPath(long sourceVertex, long targetVertex, Map<Long, Edge> edgeTo) {
        var path = new ArrayList<Long>();

        long from = coordinatesToLong(Float.NaN, Float.NaN);
        long to = targetVertex;
        path.add(to);

        while (true) {
            var edge = edgeTo.get(to);

            if (edge == null) {
                break;
            }

            from = edge.from();
            path.add(from);
            to = from;
        }

        if (from == sourceVertex) {
            Collections.reverse(path);
            return path;
        }

        return null;
    }

    private static Direction determineDirection(OSMWay way) {
        var direction = Direction.UNKNOWN;

        for (var tag : way.tags()) {
            direction = determineDirection(tag);
            if (direction != Direction.UNKNOWN) break;
        }

        return direction;
    }

    private static Direction determineDirection(OSMTag tag) {
        return switch (tag.key()) {
            case JUNCTION -> switch (tag.value()) {
                case "roundabout" -> Direction.SINGLE;
                default -> Direction.UNKNOWN;
            };
            case ONEWAY -> switch (tag.value()) {
                case "yes", "true", "1" -> Direction.SINGLE;
                case "no", "false", "0" -> Direction.BOTH;
                case "-1" -> Direction.REVERSE;
                default -> Direction.UNKNOWN;
            };
            default -> Direction.UNKNOWN;
        };
    }

    private static EnumFlags<EdgeRole> getEdgeRoles(OSMWay way) {
        var highwayTag = way.tags().stream().filter(t -> t.key() == HIGHWAY).findFirst().orElse(null);

        if (highwayTag == null) {
            throw new IllegalArgumentException("The OSM way must be a highway or cycleway.");
        }

        var edgeRoles = new EnumFlags<EdgeRole>(false);

        boolean isCycleway = way.tags()
            .stream()
            .anyMatch(t -> t.key() == CYCLEWAY ||
                t.key() == CYCLEWAY_LEFT ||
                t.key() == CYCLEWAY_RIGHT ||
                t.key() == CYCLEWAY_BOTH);

        if (isCycleway) {
            edgeRoles.set(EdgeRole.BIKE);
        }

        boolean isFootway = way.tags().stream().anyMatch(t -> t.key() == FOOTWAY);

        if (isFootway) {
            edgeRoles.set(EdgeRole.WALK);
        }

        switch (highwayTag.value()) {
            case "motorway",
                "trunk",
                "primary",
                "secondary",
                "tertiary",
                "unclassified",
                "residential",
                "motorway_link",
                "trunk_link",
                "primary_link",
                "secondary_link",
                "tertiary_link",
                "living_street" -> edgeRoles.set(EdgeRole.CAR);
            case "cycleway" -> edgeRoles.set(EdgeRole.BIKE);
            case "footway" -> edgeRoles.set(EdgeRole.WALK);
        }

        return edgeRoles;
    }

    private int getExpectedMaxSpeed(OSMWay way) {
        var highwayTag = way.tags().stream().filter(t -> t.key() == HIGHWAY).findFirst().orElse(null);

        if (highwayTag == null) {
            throw new IllegalArgumentException("The OSM way must be a highway or cycleway.");
        }

        return switch (highwayTag.value()) {
            case "motorway", "motorway_link" -> 110;
            case "primary", "primary_link", "trunk", "trunk_link" -> 80;
            case "secondary", "secondary_link", "unclassified" -> 60;
            case "tertiary", "tertiary_link" -> 50;
            case "residential", "living_street" -> 40;
            default -> 0; // Return 0 for highways that aren't handled by Dijkstra in CAR mode.
        };
    }

    private static float calculateDistance(SlimOSMNode firstNode, SlimOSMNode secondNode) {
        var x1 = firstNode.lon();
        var y1 = firstNode.lat();
        var x2 = secondNode.lon();
        var y2 = secondNode.lat();

        return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private record Node(long vertex, float weight) implements Comparable<Node>, Serializable {
        @Override
        public int compareTo(Node other) {
            return Float.compare(weight, other.weight);
        }
    }

    private enum Direction {
        SINGLE, BOTH, REVERSE, UNKNOWN
    }
}
