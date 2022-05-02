package dialog;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class Dialog {
    private Stage stage;
    private boolean closable;
    private Consumer<DialogResult> callback;

    protected static Dialog load(String resource) throws IOException {
        var fxmlLoader = new FXMLLoader(Dialog.class.getResource(resource));
        Parent root = fxmlLoader.load();

        var diag = fxmlLoader.<Dialog>getController();

        diag.stage = new Stage();
        diag.stage.setScene(new Scene(root));
        diag.stage.setResizable(false);
        diag.stage.setOnCloseRequest(
                e -> {
                    if (!diag.closable) e.consume();
                });

        return diag;
    }

    protected void showAndWait() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    protected void show(Consumer<DialogResult> callback) {
        this.callback = callback;
        stage.initModality(Modality.NONE);
        stage.show();
    }

    @FXML
    protected void close(DialogResult result) {
        closable = true;
        stage.close();
        if (callback != null) callback.accept(result);
    }
}
