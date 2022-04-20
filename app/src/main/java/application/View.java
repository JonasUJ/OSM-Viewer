package application;

import geometry.Point;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import view.Controller;
import view.Model;

public class View {
    public View(Model model, Stage stage) throws IOException {
        var loader = new FXMLLoader(View.class.getResource("view.fxml"));

        stage.setScene(loader.load());
        Controller controller = loader.getController();
        controller.init(model);

        stage.setOnCloseRequest(event -> controller.dispose());
        stage.setTitle("OSM Viewer (OpenGL)");
        stage.show();

        controller.center(Point.geoToMap(model.bounds.center()));
    }
}
