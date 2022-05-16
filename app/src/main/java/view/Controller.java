package view;

import Search.Address;
import Search.SearchTextField;
import canvas.MapCanvas;
import canvas.Renderer;
import collections.Entity;
import com.jogamp.newt.event.MouseEvent;
import dialog.CreateMapDialog;
import dialog.LoadingDialog;
import drawing.Drawable;
import drawing.DrawableEnum;
import features.Feature;
import geometry.Point;
import geometry.Vector2D;
import io.FileParser;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import navigation.EdgeRole;
import pointsOfInterest.PointOfInterest;
import pointsOfInterest.PointsOfInterestHBox;
import pointsOfInterest.PointsOfInterestVBox;

public class Controller {
    private final Timer queryPointTimer = new Timer();
    private final Timer clearCopiedTimer = new Timer();
    private Model model;
    private TimerTask queryPointTimerTask;
    private Point fromPoint, toPoint;
    private Entity routeDrawing;
    private Pair<Entity, Entity> fromToDrawings;
    private boolean pointOfInterestMode = false;
    private Tooltip addPointOfInterestText;
    private Entity lastDrawnAddress;

    @FXML
    private MapCanvas canvas;
    private final ListChangeListener<? super Point> routeRedrawListener =
            listener -> {
                while (listener.next()) {
                }

                if (!listener.wasAdded()) {
                    return;
                }

                var renderer = canvas.getRenderer();
                if (routeDrawing != null) renderer.clear(routeDrawing);

                var vectors = listener.getAddedSubList().stream().map(Vector2D::create).toList();

                routeDrawing = renderer.draw(vectors, DrawableEnum.ROUTE);

                if (fromToDrawings != null) {
                    renderer.clear(fromToDrawings.getValue());
                    renderer.clear(fromToDrawings.getKey());
                }

                var fromPoint = model.getFromToPoints().getKey();
                var toPoint = model.getFromToPoints().getValue();

                fromToDrawings =
                        new Pair<>(
                                renderer.draw(Vector2D.create(fromPoint), DrawableEnum.ADDRESS),
                                renderer.draw(Vector2D.create(toPoint), DrawableEnum.ADDRESS));
            };
    @FXML
    private Button searchButton;
    @FXML
    private Scene scene;
    @FXML
    private Button addPointOfInterest;
    @FXML
    private HBox pointsOfInterestHeader;
    @FXML
    private VBox categories;
    @FXML
    private GridPane searchPane;
    @FXML
    private SearchTextField searchTextField;
    @FXML
    private SearchTextField fromRouteTextField;
    @FXML
    private SearchTextField toRouteTextField;
    @FXML
    private Button routeButton;
    @FXML
    private ComboBox<EdgeRole> navigationModeBox;
    @FXML
    private Label routeErrorLabel;
    @FXML
    private RadioButton radioButtonCar;
    @FXML
    private RadioButton radioButtonBikeOrWalk;
    @FXML
    private CheckBox checkBoxBuildings;
    @FXML
    private CheckBox checkBoxHighways;
    @FXML
    private CheckBox checkBoxWater;
    @FXML
    private RadioButton radioButtonColorBlind;
    @FXML
    private RadioButton radioButtonDefaultMode;
    @FXML
    private RadioButton radioButtonPartyMode;
    @FXML
    private ToggleGroup groupRoute;
    @FXML
    private ToggleGroup groupMode;
    @FXML
    private VBox leftVBox;
    @FXML
    private VBox rightVBox;
    @FXML
    private VBox middleVBox;
    @FXML
    private HBox middleHBox;
    @FXML
    private Label nearestRoadLabel;
    @FXML
    private CheckMenuItem nearestRoadDelayItem;
    @FXML
    private PointsOfInterestVBox pointsOfInterestVBox;
    @FXML
    private Label scaleBarText;
    @FXML
    private Label zoomLevelText;
    @FXML
    private Label fps;
    @FXML
    private Button instructionsButton;

    public void init(Model model) {
        setModel(model);

        setStyleSheets("style.css");

        fps.textProperty().bind(canvas.fpsProperty.asString("FPS: %.1f"));

        canvas.mapMouseClickedProperty.set(
                e -> {


                    if (e.getButton() == MouseEvent.BUTTON2) {
                        var point = new Point(e.getX(), e.getY());
                        point = canvas.canvasToMap(point);
                        point = Point.mapToGeo(point);

                        if (fromPoint == null) {
                            fromPoint = point;
                        } else if (toPoint == null) {
                            toPoint = point;

                            var mode = navigationModeBox.getValue();
                            this.model.calculateBestRoute(fromPoint, toPoint, mode);
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
                                    addPointOfInterest(new PointOfInterest(point.x(), point.y(), tf.getText()));
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

        navigationModeBox.setItems(FXCollections.observableArrayList(EdgeRole.values()));

        navigationModeBox.getSelectionModel().select(0);
        routeErrorLabel.prefWidthProperty().bind(searchPane.widthProperty());

        canvas.mapMouseWheelProperty.set(
                e -> {
                    setZoomAndScale();
                });

        // FIXME: yuck
        categories
                .getChildren()
                .addAll(
                        Arrays.stream(Drawable.Category.values())
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


        searchTextField.setOnAction(event -> {
           search();
        });
    }

    private void setModel(Model model) {
        if (this.model != null) {
            this.model.getRoutePoints().removeListener(routeRedrawListener);
        }

        disableAll();

        this.model = model;
        model.getRoutePoints().addListener(routeRedrawListener);
        routeDrawing = null;

        if (model.supports(Feature.DRAWING)) {
            canvas.setModel(model.canvasModel);
            canvas.setVisible(true);
            fps.setVisible(true);

            pointsOfInterestVBox.init(model.getPointsOfInterest());
            rightVBox.setDisable(false);
            middleHBox.setDisable(false);
            canvas.setZoomHandler(model.bounds);
            setZoomAndScale();
        }

        if (model.supports(Feature.ADDRESS_SEARCH)) {
            searchTextField.init(model);
            searchTextField.setDisable(false);
            searchButton.setDisable(false);
            lastDrawnAddress = null;

            model
                    .getObservableSearchSuggestions()
                    .addListener(
                            (ListChangeListener<Address>)
                                    c -> {
                                        searchTextField.showMenuItems((ObservableList<Address>) c.getList());
                                    });
        }

        if (model.supports(Feature.PATHFINDING) && model.supports(Feature.ADDRESS_SEARCH)) {
            fromToDrawings = null;
            toRouteTextField.init(model);
            fromRouteTextField.init(model);
            toRouteTextField.setDisable(false);
            fromRouteTextField.setDisable(false);
            routeButton.setDisable(false);
            instructionsButton.setDisable(false);
            navigationModeBox.setDisable(false);
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

        canvas.giveFocus();
    }

    public void disableAll() {
        if (model != null) model.dispose();

        canvas.dispose();
        canvas.setVisible(false);
        fps.setVisible(false);
        rightVBox.setDisable(true);

        searchTextField.setDisable(true);
        searchButton.setDisable(true);

        toRouteTextField.setDisable(true);
        fromRouteTextField.setDisable(true);
        routeButton.setDisable(true);
        instructionsButton.setDisable(true);
        navigationModeBox.setDisable(true);

        nearestRoadLabel.setVisible(false);
        middleHBox.setDisable(true);
    }

    public void dispose() {
        model.dispose();
        canvas.dispose();
        queryPointTimer.cancel();
        clearCopiedTimer.cancel();
    }


    private Address handleSearchInput(SearchTextField textField) {
        var parsedAddress = textField.parseAddress();
        var result = textField.handleSearch(parsedAddress);
        if (result == null || result.size() <= 0) {
            return null;
        }
        if (result.size() > 1) {
            routeErrorLabel.setText("More than one address was found. Try being more specific.");
            routeErrorLabel.setVisible(true);


            return null;
        }
        return result.get(0);
    }


    @FXML
    public void handleSearchClick() {
       search();
    }

    public void search(){
        var result = handleSearchInput(searchTextField);
        if (result == null) {
            routeErrorLabel.setText(
                    "Please enter valid search address."
                            + System.lineSeparator()
                            + "The format is <Street> <House Number> (<Floor> <Side>) <Postcode> and/or <City>");
            routeErrorLabel.setVisible(true);

            return;
        }

        Point point = Point.geoToMap(new Point(result.lon(), result.lat()));

        zoomOn(point);
        var drawing = canvas.getRenderer().draw(Vector2D.create(point), DrawableEnum.ADDRESS);
        if (lastDrawnAddress != null) {
            canvas.getRenderer().clear(lastDrawnAddress);
        }
        lastDrawnAddress = drawing;

        searchTextField.clear();
    }

    @FXML
    public void handleSearchInFocus() {
        searchTextField.showCurrentAddresses();
    }

    @FXML
    public void handleToInFocus() {
        toRouteTextField.showCurrentAddresses();
    }

    @FXML
    public void handleFromInFocus() {
        fromRouteTextField.showCurrentAddresses();
    }

    @FXML
    public void handleRouteClick() {
        var parsedAddress = searchTextField.parseAddress();

        var fromRouteResult = handleSearchInput(fromRouteTextField);
        var toRouteResult = handleSearchInput(toRouteTextField);


        if (fromRouteResult == null || toRouteResult == null) {
            routeErrorLabel.setText(
                    "Please enter valid from and to addresses."
                            + System.lineSeparator()
                            + "The format is <Street> <House Number> (<Floor> <Side>) <Postcode> and/or <City>");
            routeErrorLabel.setVisible(true);

            return;
        }
        routeBetweenAddresses(fromRouteResult, toRouteResult, navigationModeBox.getValue());

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
        canvas.smoothZoomTo(4,point);
    }

    public void addPointOfInterest(PointOfInterest point) {
        model.getPointsOfInterest().add(point);
        point.setDrawing(
                canvas.getRenderer().draw(Vector2D.create(point.lon(), point.lat()), DrawableEnum.POI));
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
                                    canvas.getRenderer().clear(hBox.getPointOfInterest().getDrawing());
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
        addPointOfInterestText.show(rightVBox, screenBounds.getMinX(), screenBounds.getMinY() + 360);

        pointOfInterestMode = true;
    }

    @FXML
    public void handleInstructions() {
        if (!model.getInstructionsFromDijkstra().equals("")){
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(model.getInstructionsFromDijkstra());
            clipboard.setContent(content);
            instructionsButton.setText("Copied!");

            var clearCopiedTimerTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> instructionsButton.setText("Copy route"));
                    }
                };
            clearCopiedTimer.schedule(clearCopiedTimerTask, 2000);
        }
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
                                "OSM data file", "*.osm", "*.xml", "*.osm.zip", "*.xml.zip", "*.zip"));
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
        routeErrorLabel.setVisible(false);
        var result = handleKeyTyped(event);
        if (result == null) {
            model.setFromSuggestions(Collections.emptyList());
            return;
        }
        model.setFromSuggestions(result);
    }

    @FXML
    public void handleToKeyTyped(KeyEvent event) {
        routeErrorLabel.setVisible(false);

        var result = handleKeyTyped(event);
        if (result == null) {
            model.setToSuggestions(Collections.emptyList());
            return;
        }
        model.setToSuggestions(result);
    }

    @FXML
    public void handleSearchKeyTyped(KeyEvent event) {
        var result = handleKeyTyped(event);

        if (result == null) {
            model.setSearchSuggestions(Collections.emptyList());
            return;
        }

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
        Point from = new Point(addressFrom.lon(), addressFrom.lat());
        Point to = new Point(addressTo.lon(), addressTo.lat());

        var hasRoute = model.calculateBestRoute(from, to, mode);

        if (!hasRoute) {
            routeErrorLabel.setText("No route could be found.");
            routeErrorLabel.setVisible(true);
        } else {
            zoomOn(Point.geoToMap(from));
        }
    }

    private void setZoomAndScale() {
        zoomLevelText.setText(canvas.updateZoom());
        scaleBarText.setText(canvas.updateScalebar());
    }
}
