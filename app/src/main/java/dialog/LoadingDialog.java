package dialog;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;

import static util.TimeFormat.formatDuration;

public class LoadingDialog extends Dialog {
    @FXML
    private Label header;
    @FXML
    private Label timer;
    @FXML
    private Button closeBtn;
    @FXML
    private ProgressBar progress;

    public static void showDialog(String header, Consumer<ProgressBar> task) throws IOException {
        var diag = (LoadingDialog) load("LoadingDialog.fxml");

        var start = LocalTime.now();
        var timeline =
                new Timeline(
                        new KeyFrame(
                                javafx.util.Duration.millis(100),
                                e ->
                                        diag.timer
                                                .textProperty()
                                                .set(formatDuration(Duration.between(start, LocalTime.now())))));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        var thread =
                new Thread(
                        () -> {
                            task.accept(diag.progress);
                            Platform.runLater(
                                    () -> {
                                        diag.closeBtn.setDisable(false);
                                        diag.progress.setProgress(100);
                                        diag.header.textProperty().set("Done");
                                        timeline.stop();
                                    });
                        });
        thread.setDaemon(true);
        thread.start();

        diag.header.textProperty().set(header);

        diag.showAndWait();
    }

    @FXML
    private void initialize() {
    }

    public void finish() {
        super.close(null);
    }
}
