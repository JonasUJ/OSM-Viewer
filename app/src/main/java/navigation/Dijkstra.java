package navigation;

import static osm.elements.OSMTag.Key.*;

import collections.enumflags.EnumFlags;
import collections.spatial.LinearSearchTwoDTree;
import collections.spatial.SpatialTree;
import geometry.Point;
import geometry.Rect;
import java.io.Serializable;
import java.util.*;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;
import util.DistanceUtils;

public class Dijkstra implements OSMObserver, Serializable {
    private static Instructions instructions;
    private final Graph graph;
    private transient Rect bounds;
    private SpatialTree<Object> carTree;
    private SpatialTree<Object> bikeTree;
    private SpatialTree<Object> walkTree;

    private transient Map<Long, Float> distTo;
    private transient Map<Long, Edge> edgeTo;
    private transient Set<Long> settled;
    private transient PriorityQueue<Node> queue;
    private transient EdgeRole mode;

    public Dijkstra() {
        graph = new Graph();

        mode = EdgeRole.CAR;
    }

    private static long coordinatesToLong(Point point) {
        var lonBits = Float.floatToIntBits(point.x());
        var latBits = Float.floatToIntBits(point.y());

        return (((long) lonBits) << 32) | (latBits & 0xFFFFFFFFL);
    }

    private static Point longToCoordinates(long value) {
        var lonBits = (int) (value >>> 32);
        var latBits = (int) (value & 0xFFFFFFFFL);

        var lon = Float.intBitsToFloat(lonBits);
        var lat = Float.intBitsToFloat(latBits);

        return new Point(lon, lat);
    }

    private static List<Long> extractPath(
            long sourceVertex, long targetVertex, Map<Long, Edge> edgeTo) {
        var path = new ArrayList<Long>();
        List<Road> roads = new ArrayList<>();

        long from = coordinatesToLong(new Point(Float.NaN, Float.NaN));
        long to = targetVertex;
        path.add(to);

        while (true) {
            var edge = edgeTo.get(to);

            if (edge == null) {
                break;
            }

            from = edge.from();
            path.add(from);
            roads.add(
                    new Road(
                            edge.name(),
                            longToCoordinates(edge.from()),
                            longToCoordinates(edge.to()),
                            edge.role()));
            to = from;
        }

        // We add to the list and reverse instead of inserting at index 0, because that operation on an
        // ArrayList is slow.
        if (from == sourceVertex) {
            Collections.reverse(path);
            Collections.reverse(roads);
            instructions = new Instructions(roads);

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

        boolean isCycleway =
                way.tags().stream()
                        .anyMatch(
                                t ->
                                        t.key() == CYCLEWAY
                                                || t.key() == CYCLEWAY_LEFT
                                                || t.key() == CYCLEWAY_RIGHT
                                                || t.key() == CYCLEWAY_BOTH);

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
                    "living_street",
                    "motorway_link",
                    "trunk_link",
                    "primary_link",
                    "secondary_link",
                    "tertiary_link" -> edgeRoles.set(EdgeRole.CAR);
        }

        switch (highwayTag.value()) {
            case "primary",
                    "secondary",
                    "tertiary",
                    "unclassified",
                    "residential",
                    "living_street",
                    "track",
                    "path",
                    "cycleway",
                    "primary_link",
                    "secondary_link",
                    "tertiary_link" -> edgeRoles.set(EdgeRole.BIKE);
        }

        switch (highwayTag.value()) {
            case "primary",
                    "secondary",
                    "tertiary",
                    "unclassified",
                    "residential",
                    "living_street",
                    "track",
                    "path",
                    "cycleway",
                    "footway",
                    "pedestrian",
                    "primary_link",
                    "secondary_link",
                    "tertiary_link" -> edgeRoles.set(EdgeRole.WALK);
        }

        if (highwayTag.value().equals("service")) {
            var serviceTag = way.tags().stream().filter(t -> t.key() == SERVICE).findFirst().orElse(null);

            if (serviceTag != null) {
                switch (serviceTag.value()) {
                    case "parking_aisle", "driveway" -> edgeRoles.set(EdgeRole.CAR);
                }

                switch (serviceTag.value()) {
                    case "parking_aisle", "driveway", "alley" -> {
                        edgeRoles.set(EdgeRole.BIKE);
                        edgeRoles.set(EdgeRole.WALK);
                    }
                }
            }
        }

        return edgeRoles;
    }

    @Override
    public void onBounds(Rect bounds) {
        this.bounds = bounds;

        carTree = new LinearSearchTwoDTree<>(1000, bounds);
        bikeTree = new LinearSearchTwoDTree<>(1000, bounds);
        walkTree = new LinearSearchTwoDTree<>(1000, bounds);
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

        int maxSpeed =
                tags.stream()
                        .filter(
                                t ->
                                        t.key() == MAXSPEED
                                                && !t.value().equals("signals")
                                                && !t.value().equals("none"))
                        .map(t -> Integer.parseInt(t.value()))
                        .findFirst()
                        .orElse(getExpectedMaxSpeed(way));

        if (edgeRoles.isSet(EdgeRole.CAR) && maxSpeed == 0) {
            throw new RuntimeException("Max speed cannot be zero when edge is used for CAR mode.");
        }

        var direction = determineDirection(way);

        if (direction == Direction.UNKNOWN) {
            direction = Direction.BOTH;
        }

        var nodes = way.nodes();
        var firstNode = nodes[0];

        String name =
                tags.stream()
                        .filter(t -> t.key() == NAME)
                        .map(OSMTag::value)
                        .findFirst()
                        .orElse("Unnamed way");

        var roadRole = getRoadRole(way);

        for (int i = 1; i < nodes.length; i++) {
            var secondNode = nodes[i];

            var firstPoint = new Point((float) firstNode.lon(), (float) firstNode.lat());
            var secondPoint = new Point((float) secondNode.lon(), (float) secondNode.lat());

            if (!bounds.contains(firstPoint) || !bounds.contains(secondPoint)) {
                continue;
            }

            var firstVertex = coordinatesToLong(firstPoint);
            var secondVertex = coordinatesToLong(secondPoint);
            var distance = (float) DistanceUtils.calculateEarthDistance(firstPoint, secondPoint);

            if (direction == Direction.SINGLE || direction == Direction.BOTH) {
                var edge =
                        new Edge(firstVertex, secondVertex, distance, maxSpeed, edgeRoles, roadRole, name);
                graph.addEdge(edge);
            }

            if (direction == Direction.REVERSE || direction == Direction.BOTH) {
                var edge =
                        new Edge(secondVertex, firstVertex, distance, maxSpeed, edgeRoles, roadRole, name);
                graph.addEdge(edge);
            }

            for (var edgeRole : EdgeRole.values()) {
                if (edgeRoles.isSet(edgeRole)) {
                    var tree =
                            switch (edgeRole) {
                                case CAR -> carTree;
                                case BIKE -> bikeTree;
                                case WALK -> walkTree;
                            };

                    tree.insert(firstPoint, null);
                    tree.insert(secondPoint, null);
                }
            }

            firstNode = secondNode;
        }
    }

    public List<Point> shortestPath(Point from, Point to, EdgeRole mode) {
        this.mode = mode;

        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
        settled = new HashSet<>();
        queue = new PriorityQueue<>();

        var tree =
                switch (mode) {
                    case CAR -> carTree;
                    case BIKE -> bikeTree;
                    case WALK -> walkTree;
                };

        var fromResult = tree.nearest(from);
        var toResult = tree.nearest(to);

        from = fromResult.point();
        to = toResult.point();

        var sourceVertex = Dijkstra.coordinatesToLong(from);
        var targetVertex = Dijkstra.coordinatesToLong(to);

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

        return (float) heuristic;
    }

    public String getInstructions() {
        if (instructions != null) {
            return instructions.getInstructionsString();
        }
        return "";
    }

    private int getExpectedMaxSpeed(OSMWay way) {
        var highwayTag = way.tags().stream().filter(t -> t.key() == HIGHWAY).findFirst().orElse(null);

        if (highwayTag == null) {
            throw new IllegalArgumentException("The OSM way must be a highway or cycleway.");
        }

        var maxSpeed =
                switch (highwayTag.value()) {
                    case "motorway" -> 110;
                    case "motorway_link" -> 100;
                    case "primary", "trunk" -> 80;
                    case "primary_link", "trunk_link" -> 70;
                    case "secondary", "unclassified" -> 60;
                    case "tertiary", "secondary_link" -> 50;
                    case "residential", "living_street", "tertiary_link" -> 40;
                    default -> 0; // Return 0 for highways that aren't handled by Dijkstra in CAR mode.
                };

        if (maxSpeed == 0) {
            var serviceTag = way.tags().stream().filter(t -> t.key() == SERVICE).findFirst().orElse(null);

            if (serviceTag != null) {
                maxSpeed =
                        switch (serviceTag.value()) {
                            case "parking_aisle" -> 15;
                            case "driveway" -> 20;
                            default -> 0; // Return 0 for service roads that aren't handled by Dijkstra in CAR
                                // mode.
                        };
            }
        }

        return maxSpeed;
    }

    private RoadRole getRoadRole(OSMWay way) {
        var tags = way.tags().stream().filter(t -> t.key() == HIGHWAY).findFirst().orElse(null);
        var tagsRoundabout =
                way.tags().stream().filter(t -> t.key() == JUNCTION).findFirst().orElse(null);
        if (tagsRoundabout == null) {
            return switch (tags.value()) {
                case "motorway" -> RoadRole.MOTORWAY;
                case "motorway_link" -> RoadRole.MOTORWAYLINK;
                case "primary_link", "trunk_link", "tertiary_link", "secondary_link" -> RoadRole.LINK;
                case "mini_roundabout" -> RoadRole.ROUNDABOUT;
                default -> RoadRole.WAY;
            };
        } else {
            return switch (tagsRoundabout.value()) {
                case "roundabout" -> RoadRole.ROUNDABOUT;
                default -> RoadRole.WAY;
            };
        }
    }

    private enum Direction {
        SINGLE,
        BOTH,
        REVERSE,
        UNKNOWN
    }

    private record Node(long vertex, float weight) implements Comparable<Node>, Serializable {
        @Override
        public int compareTo(Node other) {
            return Float.compare(weight, other.weight);
        }
    }
}
