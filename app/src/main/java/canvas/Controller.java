package canvas;

import Search.SearchTextField;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import drawing.Category;
import drawing.Drawable;
import drawing.Drawing;
import geometry.Point;
import geometry.Vector2D;

import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pointsOfInterest.PointOfInterest;
import pointsOfInterest.PointsOfInterestHBox;
import pointsOfInterest.PointsOfInterestVBox;

public class Controller implements MouseListener {
    public Menu categories;
    private Point2D lastMouse;
    private ScaleBar scaleBar = new ScaleBar();
    private float currentScale;
    private float zoomLevel = 0;
    Model model;

    @FXML private MapCanvas canvas;

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

    @FXML private BorderPane borderPane;

    @FXML private VBox middleVBox;

    @FXML private VBox rightVBox;
    
    @FXML private VBox leftVBox;

    @FXML private Label scaleBarText;

    @FXML private Rectangle scaleBarRectangle;

    @FXML private Label zoomLevelText;

    @FXML private PointsOfInterestVBox pointsOfInterestVBox;

    boolean pointOfInterestMode = false;
    Tooltip addPointOfInterestText;

    Drawing lastDrawnAddress;

    public void init(Model model) {
        this.model=model;
        canvas.init(model);
        canvas.addMouseListener(this);
        searchTextField.init(model.getAddresses());
        checkBoxBuildings.setSelected(true);
        checkBoxHighways.setSelected(true);
        checkBoxWater.setSelected(true);
        radioButtonDefaultMode.setSelected(true);
        radioButtonCar.setSelected(true);
        setStyleSheets("style.css");
        initScaleBar();
        zoomLevel = (float) ((canvas.getZoom() - canvas.getStartZoom())*100);
        zoomLevelText.setText(Float.toString((float) (Math.round(zoomLevel*100.0)/100.0)) + "%");
        pointsOfInterestVBox.init(model.getPointsOfInterest());
        
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

    public void dispose() {
        canvas.dispose();
    }

    @FXML void handleZoomInButton(){
        canvas.zoomChange(true);
        updateZoom();
        canvas.center(canvas.canvasToMap(new Point(1280/2, 720/2)));
        
    }

    @FXML void handleZoomOutButton(){
        canvas.zoomChange(false);
        updateZoom();
        canvas.center(canvas.canvasToMap(new Point(1280/2, 720/2)));
    }

    @FXML
    public void handleKeyTyped() {
        searchTextField.handleSearchChange();
    }

    @FXML
    public void handleSearchClick() {
        var address = searchTextField.handleSearch();
        if (address == null) return; //TODO: handle exception and show message?
        Point point = Point.geoToMap(new Point((float)address.node().lon(),(float)address.node().lat()));
        zoomOn(point);
        var drawing=Drawing.create(new Vector2D(point), Drawable.ADDRESS);
        canvas.getRenderer().draw(drawing);
        if (lastDrawnAddress!=null){
            canvas.getRenderer().clear(lastDrawnAddress);
        }
        lastDrawnAddress=drawing;

    }

    @FXML
    public void handleInFocus(){
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

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

      if (pointOfInterestMode){
          Point point = canvas.canvasToMap(new Point((float)mouseEvent.getX(),(float)mouseEvent.getY()));
          var cm = new ContextMenu();
          var tf = new TextField("POI name");
          var mi = new CustomMenuItem(tf);
          mi.setHideOnClick(false);
          cm.getItems().add(mi);

          cm.show(canvas, Side.LEFT, mouseEvent.getX(), mouseEvent.getY());
          tf.requestFocus();
          canvas.giveFocus();

          tf.setOnAction(e -> {
              addPointOfInterest(new PointOfInterest(point.x(),point.y(),tf.getText(),Drawing.create(new Vector2D(point),Drawable.POI)));
              cm.hide();
          });
        pointOfInterestMode=false;
        addPointOfInterestText.hide();

      }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if (pointOfInterestMode){
            var bounds =canvas.getBoundsInLocal();
            var screenBounds=canvas.localToScreen(bounds);
            addPointOfInterestText.show(canvas,mouseEvent.getX()+screenBounds.getMinX()+50, mouseEvent.getY()+screenBounds.getMinY()-30);
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        canvas.pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());

    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        if (mouseEvent.getRotation()[0] == 0.0) {
            canvas.zoom(
                    (float) Math.pow(1.05, mouseEvent.getRotation()[1]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        } else {
            canvas.zoom(
                    (float) Math.pow(1.15, mouseEvent.getRotation()[0]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
        updateZoom(); 
    }

    public void updateZoom(){
        handleScaleBar();
        zoomLevel = (float) ((canvas.getZoom() - canvas.getStartZoom())*100);
        zoomLevelText.setText(Float.toString((float) (Math.round(zoomLevel*100.0)/100.0)) + "%");
    }

    public void initScaleBar(){
        currentScale = (float) (scaleBar.getScaleBarDistance(
            model.bounds.getBottomLeft().x(), 
            model.bounds.getBottomLeft().y(), 
            model.bounds.getBottomRight().x(), 
            model.bounds.getBottomRight().y()) * (100/canvas.getPrefWidth()));
        handleScaleBar();
    }

    public void handleScaleBar(){
        var newScale = (float) (currentScale *  (1/canvas.getZoom()));
        if (newScale < 1000){
            scaleBarText.setText(Float.toString((float) (Math.round(newScale*100.0)/100.0)) + "m");
        } else {
            scaleBarText.setText(Float.toString((float) (Math.round((newScale/1000)*100.0)/100.0)) + "km");
        } 
    }

    public void setStyleSheets(String stylesheet) {
        borderPane.getStylesheets().clear();
        borderPane.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
        middleVBox.getStylesheets().clear();
        middleVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
    }

    public void center(Point point) {
        System.out.println(point);
        canvas.center(point);
    }
    public void zoomOn(Point point) {
        canvas.zoomOn(point);
    }

    public void addPointOfInterest(PointOfInterest point){
        model.getPointsOfInterest().add(point);
        canvas.getRenderer().draw(point.drawing());
        pointsOfInterestVBox.update();
        for (Node n:pointsOfInterestVBox.getChildren()){

            if (((PointsOfInterestHBox)n).getPointOfInterest()==point){
               var hBox = (PointsOfInterestHBox)n;
               hBox.getFind().setOnAction(e -> {
                  zoomOn(new Point(hBox.getPointOfInterest().lon(),hBox.getPointOfInterest().lat()));
               });
               hBox.getRemove().setOnAction(e -> {
                    model.getPointsOfInterest().remove(hBox.getPointOfInterest());
                    canvas.getRenderer().clear(hBox.getPointOfInterest().drawing());
                    pointsOfInterestVBox.update();
               });
            }
        }
    }

    @FXML
    public void enterPointOfInterestMode(ActionEvent actionEvent) {

        if (addPointOfInterestText==null){
            addPointOfInterestText=new Tooltip("Place point of interest on map");
            addPointOfInterestText.requestFocus();
            canvas.giveFocus();
        }
        var bounds =rightVBox.getBoundsInLocal();
        var screenBounds=rightVBox.localToScreen(bounds);
        addPointOfInterestText.show(rightVBox,screenBounds.getMinX(),screenBounds.getMinY()+230);

        pointOfInterestMode=true;
    }
}
