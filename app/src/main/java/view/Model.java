package view;

import Search.AddressDatabase;
import features.Feature;
import geometry.Point;
import geometry.Rect;
import io.PolygonsReader;
import io.ReadResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import navigation.NearestNeighbor;
import pointsOfInterest.PointOfInterest;

public class Model {
    public final Rect bounds;
    private AddressDatabase addresses;
    private final List<PointOfInterest> pointsOfInterest;
    private NearestNeighbor nearestNeighbor;
    private final StringProperty nearestRoad = new SimpleStringProperty("none");
    private final Set<Feature> features;

    public canvas.Model canvasModel;

    public Model(ReadResult result) {
        bounds = result.bounds().getRect();

        features = result.readers().keySet();

        for (var entry : result.readers().entrySet()) {
            switch (entry.getKey()) {
                case DRAWING -> canvasModel = new canvas.Model((PolygonsReader) entry.getValue());
                case NEAREST_NEIGHBOR -> nearestNeighbor = (NearestNeighbor) entry.getValue().read();
                case PATHFINDING -> {}
                case ADDRESS_SEARCH -> {
                    addresses = (AddressDatabase) entry.getValue().read();
                    // FIXME: Why are the tries not built at the .map file creation step?
                    addresses.buildTries();
                }
            }
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

    public void setQueryPoint(Point query) {
        var road = nearestNeighbor.nearestTo(query);
        nearestRoadProperty().set(road);
    }

    public AddressDatabase getAddresses() {
        return addresses;
    }

    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }
}
