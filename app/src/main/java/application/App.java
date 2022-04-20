package application;

import features.FeatureSet;
import io.FileParser;
import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;
import view.Model;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        // TODO: Exchange below lines before we submit (examiner, if you're reading this, I'm sorry)
        // var model = new Model(FileParser.readMap(new File("data/bornholm.map")));
        var model =
                new Model(
                        FileParser.readMap(
                                FileParser.createMapFromOsm(new File("data/bornholm.xml.zip"), FeatureSet.ALL)));

        new View(model, primaryStage);
    }
}
