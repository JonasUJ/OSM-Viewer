package dialog;

import features.Feature;
import features.FeatureSet;
import io.FileParser;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javax.xml.stream.XMLStreamException;
import osm.ReaderStats;

public class CreateMapDialog extends Dialog {
    private Set<Feature> set;
    private File file;

    @FXML private Label header;
    @FXML private VBox checkboxes;
    @FXML private GridPane statsGrid;
    @FXML private Button next;
    @FXML private Button cancel;
    @FXML private Label nodeTotal;
    @FXML private Label nodeThroughput;
    @FXML private Label wayTotal;
    @FXML private Label wayThroughput;
    @FXML private Label relationTotal;
    @FXML private Label relationThroughput;

    private void setSet(Set<Feature> set) {
        this.set = set;
        checkboxes.getChildren().clear();

        for (var e : set) {
            var cb = new CheckBox(e.toString());
            cb.setSelected(true);
            cb.selectedProperty()
                    .addListener(
                            (observable, oldValue, newValue) -> {
                                if (newValue) this.set.add(e);
                                else this.set.remove(e);
                            });

            checkboxes.getChildren().add(cb);
        }
    }

    public static File showDialog(File file) throws IOException {
        var diag = (CreateMapDialog) load("CreateMapDialog.fxml");

        diag.setSet(EnumSet.allOf(Feature.class));
        diag.file = file;

        diag.showAndWait();

        return diag.file;
    }

    @FXML
    private void next(ActionEvent actionEvent) {
        header.textProperty().set("Creating map...");
        checkboxes.setDisable(true);
        statsGrid.setVisible(true);
        cancel.setDisable(true);
        next.setDisable(true);
        next.onActionProperty().set(e -> close());
        next.textProperty().set("Open");

        var stats = new ReaderStats(1_000_000_000);
        nodeTotal.textProperty().bind(Bindings.concat(stats.nodeTotal));
        wayTotal.textProperty().bind(Bindings.concat(stats.wayTotal));
        relationTotal.textProperty().bind(Bindings.concat(stats.relationTotal));
        nodeThroughput.textProperty().bind(Bindings.concat(stats.nodeThroughput, " per second"));
        wayThroughput.textProperty().bind(Bindings.concat(stats.wayThroughput, " per second"));
        relationThroughput
                .textProperty()
                .bind(Bindings.concat(stats.relationThroughput, " per second"));

        var featureSet = new FeatureSet(set);

        var thread =
                new Thread(
                        () -> {
                            try {
                                file = FileParser.createMapFromOsm(file, featureSet, stats);
                                Platform.runLater(
                                        () -> {
                                            next.setDisable(false);
                                            cancel.setDisable(false);
                                        });
                            } catch (IOException | XMLStreamException e) {
                                throw new RuntimeException(e);
                            }
                        });

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        file = null;
        close();
    }
}
