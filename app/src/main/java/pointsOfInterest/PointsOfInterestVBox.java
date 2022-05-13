package pointsOfInterest;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class PointsOfInterestVBox extends VBox {

    private ObservableList<PointOfInterest> pointsOfInterest;

    public void init(ObservableList<PointOfInterest> points) {

        pointsOfInterest = points;
        points.addListener(
                (ListChangeListener<? super PointOfInterest>)
                        listener -> {
                            update();
                        });

        update();
    }

    public void update() {
        removeDeletedPoints();
        addNewPointsOfInterest();
    }

    private void addNewPointsOfInterest() {
        for (PointOfInterest point : pointsOfInterest) {
            if (!contains(point)) {
                addPointOfInterest(point);
            }
        }
    }

    private void removeDeletedPoints() {
        var toToBeRemoved = new ArrayList<PointsOfInterestHBox>();
        for (Node n : getChildren()) {
            if (!pointsOfInterest.contains(((PointsOfInterestHBox) n).getPointOfInterest())) {
                toToBeRemoved.add((PointsOfInterestHBox) n);
            }
        }
        for (PointsOfInterestHBox point : toToBeRemoved) {
            removePointOfInterest(point.getPointOfInterest());
        }
    }

    public void addPointOfInterest(PointOfInterest point) {
        getChildren().add(new PointsOfInterestHBox(point));
    }

    public void removePointOfInterest(PointOfInterest point) {
        getChildren().removeIf(n -> ((PointsOfInterestHBox) n).getPointOfInterest() == point);
    }

    public boolean contains(PointOfInterest point) {
        for (Node n : getChildren()) {
            if (((PointsOfInterestHBox) n).getPointOfInterest() == point) {
                return true;
            }
        }
        return false;
    }
}
