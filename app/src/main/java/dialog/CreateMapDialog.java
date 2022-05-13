package dialog;

import features.Feature;
import features.FeatureSet;
import io.FileParser;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import osm.ReaderStats;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import static util.TimeFormat.formatDuration;

public class CreateMapDialog extends Dialog {
    private Set<Feature> set;
    private File file;

    @FXML
    private Label header;
    @FXML
    private VBox checkboxes;
    @FXML
    private GridPane statsGrid;
    @FXML
    private Button next;
    @FXML
    private Label nodeTotal;
    @FXML
    private Label nodeThroughput;
    @FXML
    private Label wayTotal;
    @FXML
    private Label wayThroughput;
    @FXML
    private Label relationTotal;
    @FXML
    private Label relationThroughput;
    @FXML
    private ProgressBar progress;
    @FXML
    private Label timer;

    public static File showDialog(File file) throws IOException {
        var diag = (CreateMapDialog) load("CreateMapDialog.fxml");

        diag.setSet(EnumSet.allOf(Feature.class));
        diag.file = file;

        diag.showAndWait();

        return diag.file;
    }

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

    @FXML
    private void next() {
        header.textProperty().set("Creating map...");
        checkboxes.setDisable(true);
        statsGrid.setVisible(true);
        next.setDisable(true);
        next.onActionProperty().set(e -> close(new CreateMapDialogResult(file)));
        next.textProperty().set("Open");

        var start = LocalTime.now();
        var timeline =
                new Timeline(
                        new KeyFrame(
                                javafx.util.Duration.millis(100),
                                e ->
                                        timer
                                                .textProperty()
                                                .set(formatDuration(Duration.between(start, LocalTime.now())))));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        progress.setVisible(true);
        timer.setVisible(true);

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
                                file = FileParser.createMapFromOsm(file, featureSet, progress, stats);
                                Platform.runLater(
                                        () -> {
                                            next.setDisable(false);
                                            header.textProperty().set(file.getName() + " created");
                                            progress.setProgress(100);
                                            timeline.stop();
                                        });
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

        thread.setDaemon(true);
        thread.start();
    }
}
