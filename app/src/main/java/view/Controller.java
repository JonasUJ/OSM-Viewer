package view;

import Search.SearchTextField;
import canvas.MapCanvas;
import canvas.Renderer;
import dialog.CreateMapDialog;
import drawing.Category;
import drawing.Drawable;
import drawing.Drawing;
import features.Feature;
import geometry.Point;
import geometry.Vector2D;
import io.FileParser;
import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import pointsOfInterest.PointOfInterest;
import pointsOfInterest.PointsOfInterestHBox;
import pointsOfInterest.PointsOfInterestVBox;

public class Controller {
    private Model model;
    private final Timer queryPointTimer = new Timer();
    private TimerTask queryPointTimerTask;
    private boolean pointOfInterestMode = false;
    private Tooltip addPointOfInterestText;
    private Drawing lastDrawnAddress;

    @FXML private MapCanvas canvas;

    @FXML private Button searchButton;

    @FXML private Scene scene;

    @FXML private Button addPointOfInterest;

    @FXML private HBox pointsOfInterestHeader;

    @FXML private Menu categories;

    @FXML private SearchTextField searchTextField;

    @FXML private TextField fromRouteTextField;

    @FXML private TextField toRouteTextField;

    @FXML private Button routeButton;

    @FXML private RadioButton radioButtonCar;

    @FXML private RadioButton radioButtonBikeOrWalk;

    @FXML private CheckBox checkBoxBuildings;

    @FXML private CheckBox checkBoxHighways;

    @FXML private CheckBox checkBoxWater;

    @FXML private RadioButton radioButtonColorBlind;

    @FXML private RadioButton radioButtonDefaultMode;

    @FXML private RadioButton radioButtonPartyMode;

    @FXML private ToggleGroup groupRoute;

    @FXML private ToggleGroup groupMode;

    @FXML private VBox leftVBox;

    @FXML private VBox rightVBox;

    @FXML private Label nearestRoadLabel;

    @FXML private CheckMenuItem nearestRoadDelayItem;

    @FXML private PointsOfInterestVBox pointsOfInterestVBox;

    public void init(Model model) {
        setModel(model);

        setStyleSheets("style.css");

        canvas.mapMouseClickedProperty.set(
                e -> {
                    if (pointOfInterestMode) {
                        Point point = canvas.canvasToMap(new Point((float) e.getX(), (float) e.getY()));
                        var cm = new ContextMenu();
                        var tf = new TextField("POI name");
                        var mi = new CustomMenuItem(tf);
                        mi.setHideOnClick(false);
                        cm.getItems().add(mi);

                        cm.show(canvas, Side.LEFT, e.getX(), e.getY());
                        tf.requestFocus();
                        canvas.giveFocus();

                        tf.setOnAction(
                                a -> {
                                    addPointOfInterest(
                                            new PointOfInterest(
                                                    point.x(),
                                                    point.y(),
                                                    tf.getText(),
                                                    Drawing.create(new Vector2D(point), Drawable.POI)));
                                    cm.hide();
                                });

                        pointOfInterestMode = false;
                        addPointOfInterestText.hide();
                    }
                });

        canvas.mapMouseMovedProperty.set(
                e -> {
                    Runnable queryRunnable =
                            () -> {
                                var mousePoint = new Point(e.getX(), e.getY());
                                var queryPoint = canvas.canvasToMap(mousePoint);
                                if (this.model.supports(Feature.NEAREST_NEIGHBOR)) {
                                    this.model.setQueryPoint(queryPoint);
                                }
                            };

                    if (nearestRoadDelayItem.isSelected()) {
                        if (queryPointTimerTask != null) {
                            queryPointTimerTask.cancel();
                        }

                        queryPointTimerTask =
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        Platform.runLater(queryRunnable);
                                    }
                                };
                        queryPointTimer.schedule(queryPointTimerTask, 50);
                    } else {
                        queryRunnable.run();
                    }

                    if (pointOfInterestMode) {
                        var bounds = canvas.getBoundsInLocal();
                        var screenBounds = canvas.localToScreen(bounds);
                        addPointOfInterestText.show(
                                canvas,
                                e.getX() + screenBounds.getMinX() + 50,
                                e.getY() + screenBounds.getMinY() - 30);
                    }
                });

        // FIXME: yuck
        categories
                .getItems()
                .addAll(
                        Arrays.stream(Category.values())
                                .map(
                                        c -> {
                                            var cb = new CheckBox(c.toString());
                                            cb.setStyle("-fx-text-fill: #222222");
                                            cb.selectedProperty().set(canvas.categories.isSet(c));

                                            canvas.categories.addObserver(
                                                    e -> {
                                                        if (e.variant() == c) {
                                                            cb.setSelected(e.enabled());
                                                        }
                                                    });

                                            cb.selectedProperty()
                                                    .addListener(
                                                            ((observable, oldValue, newValue) -> {
                                                                if (newValue) canvas.categories.set(c);
                                                                else canvas.categories.unset(c);
                                                            }));

                                            var m = new CustomMenuItem(cb);
                                            m.setHideOnClick(false);

                                            return m;
                                        })
                                .toList());
    }

    private void setModel(Model model) {
        if (this.model != null) this.model.dispose();

        this.model = model;

        if (model.supports(Feature.DRAWING)) {
            canvas.setModel(model.canvasModel);
            canvas.setVisible(true);

            pointsOfInterestVBox.init(model.getPointsOfInterest());
            rightVBox.setDisable(false);
            categories.setDisable(false);
        } else {
            canvas.dispose();
            canvas.setVisible(false);
            rightVBox.setDisable(true);
            categories.setDisable(true);
        }

        if (model.supports(Feature.ADDRESS_SEARCH)) {
            searchTextField.init(model.getAddresses());
            searchTextField.setDisable(false);
        } else {
            searchTextField.setDisable(true);
        }

        if (model.supports(Feature.NEAREST_NEIGHBOR)) {
            nearestRoadLabel
                    .textProperty()
                    .bind(Bindings.concat("Nearest road: ", model.nearestRoadProperty()));
            nearestRoadLabel.setVisible(true);
        } else {
            nearestRoadLabel.setVisible(false);
        }
    }

    public void dispose() {
        model.dispose();
        canvas.dispose();
        queryPointTimer.cancel();
    }

    @FXML
    public void handleKeyTyped() {
        searchTextField.handleSearchChange();
    }

    @FXML
    public void handleSearchClick() {
        var address = searchTextField.handleSearch();
        if (address == null) return; // TODO: handle exception and show message?
        Point point =
                Point.geoToMap(new Point((float) address.node().lon(), (float) address.node().lat()));
        zoomOn(point);
        var drawing = Drawing.create(new Vector2D(point), Drawable.ADDRESS);
        canvas.getRenderer().draw(drawing);
        if (lastDrawnAddress != null) {
            canvas.getRenderer().clear(lastDrawnAddress);
        }
        lastDrawnAddress = drawing;
    }

    @FXML
    public void handleInFocus() {
        searchTextField.showHistory();
    }

    @FXML
    public void handleRouteClick() {
        fromRouteTextField.clear();
        toRouteTextField.clear();
    }

    @FXML
    public void handleDefaultMode() {
        if (radioButtonDefaultMode.isSelected()) {
            setStyleSheets("style.css");
            canvas.setShader(Renderer.Shader.DEFAULT);
        }
    }

    @FXML
    public void handleColorblind() {
        if (radioButtonColorBlind.isSelected()) {
            setStyleSheets("colorblindStyle.css");
            canvas.setShader(Renderer.Shader.MONOCHROME);
        }
    }

    @FXML
    public void handlePartyMode() {
        if (radioButtonPartyMode.isSelected()) {
            setStyleSheets("partyStyle.css");
            canvas.setShader(Renderer.Shader.PARTY);
        }
    }

    public void setStyleSheets(String stylesheet) {
        leftVBox.getStylesheets().clear();
        leftVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
        rightVBox.getStylesheets().clear();
        rightVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
    }

    public void center(Point point) {
        canvas.center(point);
    }

    public void zoomOn(Point point) {
        canvas.zoomTo(point);
    }

    public void addPointOfInterest(PointOfInterest point) {
        model.getPointsOfInterest().add(point);
        canvas.getRenderer().draw(point.drawing());
        pointsOfInterestVBox.update();
        for (Node n : pointsOfInterestVBox.getChildren()) {

            if (((PointsOfInterestHBox) n).getPointOfInterest() == point) {
                var hBox = (PointsOfInterestHBox) n;
                hBox.getFind()
                        .setOnAction(
                                e -> {
                                    zoomOn(
                                            new Point(hBox.getPointOfInterest().lon(), hBox.getPointOfInterest().lat()));
                                });
                hBox.getRemove()
                        .setOnAction(
                                e -> {
                                    model.getPointsOfInterest().remove(hBox.getPointOfInterest());
                                    canvas.getRenderer().clear(hBox.getPointOfInterest().drawing());
                                    pointsOfInterestVBox.update();
                                });
            }
        }
    }

    @FXML
    public void enterPointOfInterestMode(ActionEvent actionEvent) {
        if (addPointOfInterestText == null) {
            addPointOfInterestText = new Tooltip("Place point of interest on map");
            addPointOfInterestText.requestFocus();
            canvas.giveFocus();
        }

        var bounds = rightVBox.getBoundsInLocal();
        var screenBounds = rightVBox.localToScreen(bounds);
        addPointOfInterestText.show(rightVBox, screenBounds.getMinX(), screenBounds.getMinY() + 230);

        pointOfInterestMode = true;
    }

    public void openMap(ActionEvent actionEvent) throws Exception {
        var diag = new FileChooser();
        diag.setTitle("Open map file");
        diag.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Map file", "*" + FileParser.EXT));
        var file = diag.showOpenDialog(scene.getWindow());

        if (file == null) return;

        loadFile(file);
    }

    public void createMap(ActionEvent actionEvent) throws Exception {
        var diag = new FileChooser();
        diag.setTitle("Open OSM data file");
        // *.zip is here because *.osm.zip doesn't work on Linux
        diag.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "OSM data file", "*.osm", " *.xml", " *.osm.zip", " *.xml.zip", "*.zip"));
        var file = diag.showOpenDialog(scene.getWindow());
        if (file == null) return;

        file = CreateMapDialog.showDialog(file);
        if (file == null) return;

        loadFile(file);
    }

    private void loadFile(File file) throws Exception {
        try (var res = FileParser.readMap(file)) {
            setModel(new Model(res));
        }
    }
}
