package pointsOfInterest;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PointsOfInterestVBox extends VBox {

    private List<PointOfInterest> pointsOfInterest;

    public void init(List<PointOfInterest> points){
        pointsOfInterest=points;

    }

    public void update(){
        removeDeletedPoints();
        addNewPointsOfInterest();
    }

    private void addNewPointsOfInterest(){
        for (PointOfInterest point : pointsOfInterest){
            if (!contains(point)){
                addPointOfInterest(point);
            }
        }
    }

    private void removeDeletedPoints(){
        var toToBeRemoved = new ArrayList<PointsOfInterestHBox>();
        for (Node n :getChildren()){
            if (!pointsOfInterest.contains(((PointsOfInterestHBox)n).getPointOfInterest())){
                toToBeRemoved.add((PointsOfInterestHBox)n);
            }
        }
        for (PointsOfInterestHBox point : toToBeRemoved){
            removePointOfInterest(point.getPointOfInterest());
        }
    }

    public void addPointOfInterest(PointOfInterest point){
        getChildren().add(new PointsOfInterestHBox(point));

    }

    public void removePointOfInterest(PointOfInterest point){
        getChildren().removeIf(n -> ((PointsOfInterestHBox) n).getPointOfInterest() == point);


    }

    public boolean contains (PointOfInterest point){
        for (Node n :getChildren()){
            if (((PointsOfInterestHBox)n).getPointOfInterest() == point ){
                return true;
            }
        }
        return false;
    }




}
