package application;

import io.FileParser;
import javafx.application.Application;
import javafx.stage.Stage;
import view.Model;

import java.io.File;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model(FileParser.readMap(new File(App.class.getResource("bornholm.map").getFile())), null);

        new View(model, primaryStage);
    }
}
