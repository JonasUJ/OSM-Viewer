package pointsOfInterest;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class PointsOfInterestHBox extends HBox {

    private final PointOfInterest pointOfInterest;
    private final Text text;
    private final Button find;
    private final Button remove;
    float buttonWidth = 90;
    float buttonFontSize = 10;

    public PointsOfInterestHBox(PointOfInterest point) {
        pointOfInterest = point;

        setMaxWidth(140);

        text = new Text(point.name());
        text.setFont(new Font(15));

        find = new Button("Find");
        remove = new Button("X");
        find.setPrefSize(buttonWidth, 10);
        remove.setPrefSize(buttonWidth, 10);
        find.setFont(new Font(buttonFontSize));
        remove.setFont(new Font(buttonFontSize));
        remove.setId("removeButton");
        getChildren().add(text);
        getChildren().add(find);
        getChildren().add(remove);
    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public Button getFind() {
        return find;
    }

    public Button getRemove() {
        return remove;
    }
}
