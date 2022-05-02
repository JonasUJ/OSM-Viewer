package view;

import Search.Address;
import Search.SearchTextField;
import canvas.MapCanvas;
import canvas.Renderer;
import com.jogamp.newt.event.MouseEvent;
import dialog.CreateMapDialog;
import dialog.LoadingDialog;
import drawing.Category;
import drawing.Drawable;
import drawing.Drawing;
import features.Feature;
import geometry.Point;
import geometry.Vector2D;
import io.FileParser;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import navigation.EdgeRole;
import pointsOfInterest.PointOfInterest;
import pointsOfInterest.PointsOfInterestHBox;
import pointsOfInterest.PointsOfInterestVBox;

public class Controller {
    private Model model;
    private final Timer queryPointTimer = new Timer();
    private TimerTask queryPointTimerTask;
    private Point fromPoint, toPoint;
    private Drawing routeDrawing;
    private boolean pointOfInterestMode = false;
    private Tooltip addPointOfInterestText;
    private Drawing lastDrawnAddress;

    @FXML private MapCanvas canvas;

    @FXML private Button searchButton;

    @FXML private Scene scene;

    @FXML private Button addPointOfInterest;

    @FXML private HBox pointsOfInterestHeader;

    @FXML private VBox categories;

    @FXML private SearchTextField searchTextField;

    @FXML private SearchTextField fromRouteTextField;

    @FXML private SearchTextField toRouteTextField;

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

    @FXML private VBox middleVBox;

    @FXML private Label nearestRoadLabel;

    @FXML private CheckMenuItem nearestRoadDelayItem;

    @FXML private PointsOfInterestVBox pointsOfInterestVBox;

    @FXML private Label scaleBarText;

    @FXML private Rectangle scaleBarRectangle;

    @FXML private Label zoomLevelText;

    @FXML private Label fps;

    public void init(Model model) {
        setModel(model);

        setStyleSheets("style.css");

        model
                .getRoutePoints()
                .addListener(
                        (ListChangeListener<? super Point>)
                                listener -> {
                                    while (listener.next()) {}

                                    if (!listener.wasAdded()) {
                                        return;
                                    }

                                    var renderer = canvas.getRenderer();
                                    if (routeDrawing != null) renderer.clear(routeDrawing);

                                    var vectors = listener.getAddedSubList().stream().map(Vector2D::create).toList();
                                    routeDrawing = Drawing.create(vectors, Drawable.ROUTE);

                                    renderer.draw(routeDrawing);
                                });

        fps.textProperty().bind(canvas.fpsProperty.asString("FPS: %.1f"));

        canvas.mapMouseClickedProperty.set(
                e -> {
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        var point = new Point(e.getX(), e.getY());
                        point = canvas.canvasToMap(point);
                        point = Point.mapToGeo(point);
                        point = model.getNearestPoint(point);

                        if (fromPoint == null) {
                            fromPoint = point;
                        } else if (toPoint == null) {
                            toPoint = point;
                            model.calculateBestRoute(fromPoint, toPoint);
                        } else {
                            fromPoint = point;
                            toPoint = null;
                        }

                        return;
                    }

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
                                                    Drawing.create(Vector2D.create(point), Drawable.POI)));
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
                                queryPoint = Point.mapToGeo(queryPoint);
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
        canvas.mapMouseWheelProperty.set(
                e -> {
                    setZoomAndScale();
                });
        canvas.setZoomHandler(model.bounds);
        setZoomAndScale();
        // FIXME: yuck
        categories
                .getChildren()
                .addAll(
                        Arrays.stream(Category.values())
                                .map(
                                        c -> {
                                            var cb = new CheckBox(c.toString());
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

                                            return cb;
                                        })
                                .toList());
    }

    private void setModel(Model model) {
        disableAll();

        this.model = model;

        if (model.supports(Feature.DRAWING)) {
            canvas.setModel(model.canvasModel);
            canvas.setVisible(true);

            pointsOfInterestVBox.init(model.getPointsOfInterest());
            rightVBox.setDisable(false);
        }

        if (model.supports(Feature.ADDRESS_SEARCH)) {
            searchTextField.init(model);
            searchTextField.setDisable(false);
            model
                    .getObservableSearchSuggestions()
                    .addListener(
                            (ListChangeListener<Address>)
                                    c -> {
                                        searchTextField.showMenuItems((ObservableList<Address>) c.getList());
                                    });
        }

        if (model.supports(Feature.PATHFINDING)) {
            toRouteTextField.init(model);
            fromRouteTextField.init(model);
            toRouteTextField.setDisable(false);
            fromRouteTextField.setDisable(false);
            model
                    .getObservableToSuggestions()
                    .addListener(
                            (ListChangeListener<Address>)
                                    c -> {
                                        toRouteTextField.showMenuItems((ObservableList<Address>) c.getList());
                                    });
            model
                    .getObservableFromSuggestions()
                    .addListener(
                            (ListChangeListener<Address>)
                                    c -> {
                                        fromRouteTextField.showMenuItems((ObservableList<Address>) c.getList());
                                    });
        }

        if (model.supports(Feature.NEAREST_NEIGHBOR)) {
            nearestRoadLabel
                    .textProperty()
                    .bind(Bindings.concat("Nearest road: ", model.nearestRoadProperty()));
            nearestRoadLabel.setVisible(true);
        }
    }

    public void disableAll() {
        if (model != null) model.dispose();

        canvas.dispose();
        canvas.setVisible(false);
        rightVBox.setDisable(true);

        searchTextField.setDisable(true);

        toRouteTextField.setDisable(true);
        fromRouteTextField.setDisable(true);

        nearestRoadLabel.setVisible(false);
    }

    public void dispose() {
        model.dispose();
        canvas.dispose();
        queryPointTimer.cancel();
    }

    @FXML
    public void handleSearchClick() {
        var result = searchTextField.handleSearch();
        if (result == null) return; // TODO: handle exception and show message?
        if (result.size() > 1) {
            // TODO: popup message
        } else if (result.size() < 1) {

        }
        var address = result.get(0);

        Point point =
                Point.geoToMap(new Point((float) address.node().lon(), (float) address.node().lat()));
        zoomOn(point);
        var drawing = Drawing.create(Vector2D.create(point), Drawable.ADDRESS);
        canvas.getRenderer().draw(drawing);
        if (lastDrawnAddress != null) {
            canvas.getRenderer().clear(lastDrawnAddress);
        }
        lastDrawnAddress = drawing;

        searchTextField.clear(); // TODO: find ud af om den skal bruges
    }

    @FXML
    public void handleInFocus() {
        searchTextField.showHistory();
    }

    @FXML
    public void handleRouteClick() {

        if (fromRouteTextField.handleSearch() == null || toRouteTextField.handleSearch() == null) {
            return;
        }
        routeBetweenAddresses(
                fromRouteTextField.handleSearch().get(0),
                toRouteTextField.handleSearch().get(0),
                EdgeRole.CAR);
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
        middleVBox.getStylesheets().clear();
        middleVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
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

    public void openMap() throws Exception {
        var diag = new FileChooser();
        diag.setTitle("Open map file");
        diag.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Map file", "*" + FileParser.EXT));
        var file = diag.showOpenDialog(scene.getWindow());

        if (file == null) return;

        loadFile(file);
    }

    public void createMap() throws Exception {
        var diag = new FileChooser();
        diag.setTitle("Open OSM data file");
        // *.zip is here because *.osm.zip doesn't work on Linux
        diag.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "OSM data file", "*.osm", " *.xml", " *.osm.zip", " *.xml.zip", "*.zip"));
        var file = diag.showOpenDialog(scene.getWindow());
        if (file == null) return;

        disableAll();

        file = CreateMapDialog.showDialog(file);
        if (file != null) {
            try {
                loadFile(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadFile(File file) throws Exception {
        AtomicReference<Model> modelRef = new AtomicReference<>();
        LoadingDialog.showDialog(
                "Loading " + file.getName(),
                bar -> {
                    try (var res = FileParser.readMap(file)) {
                        modelRef.set(new Model(res, bar));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        var model = modelRef.get();
        if (model != null) {
            setModel(model);
            center(Point.geoToMap(model.bounds.center()));
        }
    }

    @FXML
    void handleZoomOutButton() {
        canvas.zoomChange(false);
        setZoomAndScale();
    }

    @FXML
    void handleZoomInButton() {
        canvas.zoomChange(true);
        setZoomAndScale();
    }

    @FXML
    public void handleFromKeyTyped(KeyEvent event) {
        var result = handleKeyTyped(event);
        if (result == null) return;
        model.setFromSuggestions(result);
    }

    @FXML
    public void handleToKeyTyped(KeyEvent event) {
        var result = handleKeyTyped(event);
        if (result == null) return;
        model.setToSuggestions(result);
    }

    @FXML
    public void handleSearchKeyTyped(KeyEvent event) {
        var result = handleKeyTyped(event);
        if (result == null) return;
        model.setSearchSuggestions(result);
    }

    public List<Address> handleKeyTyped(KeyEvent event) {
        var textField = (SearchTextField) event.getSource();
        var searchedAddress = textField.parseAddress();
        if (searchedAddress == null) return null;
        textField.setCurrentSearch(searchedAddress);

        return model.getAddresses().possibleAddresses(searchedAddress, 5);
    }

    private void routeBetweenAddresses(Address addressFrom, Address addressTo, EdgeRole mode) {
        Point from = new Point((float) addressFrom.node().lon(), (float) addressFrom.node().lat());
        Point to = new Point((float) addressTo.node().lon(), (float) addressTo.node().lat());

        Point dijkstraFrom = model.getNearestPoint(from);
        Point dijkstraTo = model.getNearestPoint(to);

        model.calculateBestRoute(dijkstraFrom, dijkstraTo);
    }

    private void setZoomAndScale() {
        zoomLevelText.setText(canvas.updateZoom());
        scaleBarText.setText(canvas.updateScalebar());
    }
}
