package view;

import Search.Address;
import Search.AddressDatabase;
import features.Feature;
import features.FeatureSet;
import geometry.Point;
import geometry.Rect;
import io.PolygonsReader;
import io.ReadResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import navigation.Dijkstra;
import navigation.EdgeRole;
import navigation.NearestNeighbor;
import pointsOfInterest.PointOfInterest;

public class Model {
    public final Rect bounds;
    private AddressDatabase addresses;
    private final List<PointOfInterest> pointsOfInterest;
    private NearestNeighbor nearestNeighbor;
    private final StringProperty nearestRoad = new SimpleStringProperty("none");
    private Dijkstra dijkstra;
    private final ObservableList<Point> routePoints = FXCollections.observableArrayList();
    private final FeatureSet features;

    public canvas.Model canvasModel;

    private final ObservableList<Address> searchSuggestions = FXCollections.observableArrayList();
    private final ObservableList<Address> toSuggestions = FXCollections.observableArrayList();
    private final ObservableList<Address> fromSuggestions = FXCollections.observableArrayList();

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
                    // FIXME: Why are the tries not built at the .map file creation step?
                    addresses.buildTries();
                }
            }

            if (bar != null) Platform.runLater(() -> bar.setProgress(progress.incrementAndGet() / total));
        }

        pointsOfInterest = new ArrayList<>();
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

    public Point getNearestPoint(Point query) {
        return nearestNeighbor.nearestTo(query);
    }

    public void setQueryPoint(Point query) {
        var road = nearestNeighbor.nearestRoad(query);
        nearestRoadProperty().set(road);
    }

    public ObservableList<Point> getRoutePoints() {
        return routePoints;
    }

    public void calculateBestRoute(Point from, Point to) {
        // TODO: Allow user to set edge role.
        var shortestPath = dijkstra.shortestPath(from, to, EdgeRole.CAR);

        if (shortestPath == null) {
            routePoints.clear();
            System.out.println("No path between " + from + " and " + to + ".");

            return;
        }

        var routePoints = shortestPath.stream().map(Point::geoToMap).toList();
        this.routePoints.setAll(routePoints);
    }

    public AddressDatabase getAddresses() {
        return addresses;
    }

    public List<PointOfInterest> getPointsOfInterest() {
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
}
