package view;

import Search.Address;
import Search.AddressDatabase;
import features.Feature;
import features.FeatureSet;
import geometry.Point;
import geometry.Rect;
import io.PolygonsReader;
import io.ReadResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;
import navigation.Dijkstra;
import navigation.EdgeRole;
import navigation.NearestNeighbor;
import pointsOfInterest.PointOfInterest;

public class Model {
    public final Rect bounds;
    private final ObservableList<PointOfInterest> pointsOfInterest;
    private final StringProperty nearestRoad = new SimpleStringProperty("none");
    private final ObservableList<Point> routePoints = FXCollections.observableArrayList();
    private final FeatureSet features;
    private final ObservableList<Address> searchSuggestions = FXCollections.observableArrayList();
    private final ObservableList<Address> toSuggestions = FXCollections.observableArrayList();
    private final ObservableList<Address> fromSuggestions = FXCollections.observableArrayList();
    public canvas.Model canvasModel;
    private AddressDatabase addresses;
    private NearestNeighbor nearestNeighbor;
    private Dijkstra dijkstra;
    private Pair<Point, Point> fromToPoints;

    public Model(ReadResult result, ProgressBar bar) {
        bounds = result.bounds().getRect();

        features = new FeatureSet(result.readers().keySet());

        double total = result.readers().size();
        AtomicInteger progress = new AtomicInteger();

        if (total == 1 && bar != null) Platform.runLater(() -> bar.setProgress(-1));

        for (var entry : result.readers().entrySet()) {
            switch (entry.getKey()) {
                case DRAWING -> canvasModel = new canvas.Model((PolygonsReader) entry.getValue());
                case NEAREST_NEIGHBOR -> nearestNeighbor = (NearestNeighbor) entry.getValue().read();
                case PATHFINDING -> dijkstra = (Dijkstra) entry.getValue().read();
                case ADDRESS_SEARCH -> {
                    addresses = (AddressDatabase) entry.getValue().read();
                }
            }

            if (bar != null) Platform.runLater(() -> bar.setProgress(progress.incrementAndGet() / total));
        }

        pointsOfInterest = FXCollections.observableArrayList();
        addresses.setPointsOfInterest(pointsOfInterest);
    }

    public boolean supports(Feature feature) {
        return features.contains(feature);
    }

    public void dispose() {
        if (canvasModel != null) canvasModel.dispose();
    }

    public StringProperty nearestRoadProperty() {
        return nearestRoad;
    }

    public String getNearestRoad() {
        return nearestRoad.get();
    }

    public void setQueryPoint(Point query) {
        var road = nearestNeighbor.nearestRoad(query);
        nearestRoadProperty().set(road);
    }

    public String getInstructionsFromDijkstra() {
        return dijkstra.getInstructions();
    }

    public ObservableList<Point> getRoutePoints() {
        return routePoints;
    }

    public boolean calculateBestRoute(Point from, Point to, EdgeRole mode) {
        var shortestPath = dijkstra.shortestPath(from, to, mode);

        if (shortestPath == null) {
            routePoints.clear();
            System.out.println("No path between " + from + " and " + to + ".");

            return false;
        }

        setFromToPoints(new Pair<>(Point.geoToMap(from), Point.geoToMap(to)));

        var routePoints = shortestPath.stream().map(Point::geoToMap).toList();
        this.routePoints.setAll(routePoints);

        return true;
    }

    public AddressDatabase getAddresses() {
        return addresses;
    }

    public ObservableList<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public ObservableList<Address> getObservableSearchSuggestions() {
        return searchSuggestions;
    }

    public void setSearchSuggestions(List<Address> suggestions) {
        this.searchSuggestions.setAll(suggestions);
    }

    public ObservableList<Address> getObservableToSuggestions() {
        return toSuggestions;
    }

    public void setToSuggestions(List<Address> suggestions) {
        this.toSuggestions.setAll(suggestions);
    }

    public ObservableList<Address> getObservableFromSuggestions() {
        return fromSuggestions;
    }

    public void setFromSuggestions(List<Address> suggestions) {
        this.fromSuggestions.setAll(suggestions);
    }

    public Pair<Point, Point> getFromToPoints() {
        return fromToPoints;
    }

    public void setFromToPoints(Pair<Point, Point> fromToPoints) {
        this.fromToPoints = fromToPoints;
    }
}
