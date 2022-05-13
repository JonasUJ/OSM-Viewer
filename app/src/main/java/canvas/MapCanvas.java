package canvas;

import collections.enumflags.ObservableEnumFlags;
import com.jogamp.nativewindow.javafx.JFXAccessor;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import drawing.Drawable;
import geometry.Point;
import geometry.Rect;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class MapCanvas extends Region implements MouseListener {
    // TODO: Add all if necessary
    public final ObjectProperty<EventHandler<MouseEvent>> mapMouseClickedProperty =
            new SimpleObjectProperty<>();
    public final ObjectProperty<EventHandler<MouseEvent>> mapMouseMovedProperty =
            new SimpleObjectProperty<>();
    public final ObjectProperty<EventHandler<MouseEvent>> mapMouseWheelProperty =
            new SimpleObjectProperty<>();
    public final FloatProperty fpsProperty = new SimpleFloatProperty();
    public final ObservableEnumFlags<Drawable.Category> categories = new ObservableEnumFlags<>();
    final Affine transform = new Affine();
    private Animator animator;
    private GLWindow window;
    private Model model;
    private Renderer renderer;
    private float startZoom;
    private Point2D lastMouse;
    private CanvasFocusListener canvasFocusListener;
    private ZoomHandler zoomHandler;
    private Timer resizeHeightTimer = new Timer();
    private TimerTask resizeHeightTimerTask;
    private final ChangeListener<Number> HEIGHT_LISTENER =
            (observable, oldValue, newValue) -> {
                if (resizeHeightTimerTask != null) {
                    resizeHeightTimerTask.cancel();
                }

                resizeHeightTimerTask =
                        new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(
                                        () -> window.setSize(window.getWidth(), Math.max(1, newValue.intValue())));
                            }
                        };
                resizeHeightTimer.schedule(resizeHeightTimerTask, 50);
            };
    private Timer resizeWidthTimer = new Timer();
    private TimerTask resizeWidthTimerTask;
    private final ChangeListener<Number> WIDTH_LISTENER =
            (observable, oldValue, newValue) -> {
                if (resizeWidthTimerTask != null) {
                    resizeWidthTimerTask.cancel();
                }

                resizeWidthTimerTask =
                        new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(
                                        () -> {
                                            // We need to do this twice, because otherwise it does not resize on maximize
                                            // on some systems.
                                            window.setSize(Math.max(1, newValue.intValue()), window.getHeight());
                                            window.setSize(Math.max(1, newValue.intValue()), window.getHeight());
                                        });
                            }
                        };
                resizeWidthTimer.schedule(resizeWidthTimerTask, 50);
            };
    private Timeline fpsUpdater;

    public void setModel(Model model) {
        dispose();

        this.model = model;

        // Boilerplate to let us use OpenGL from a JavaFX node hierarchy
        Platform.setImplicitExit(true);
        window = GLWindow.create(model.getCaps());
        window.setSharedAutoDrawable(model.getSharedDrawable());
        final var canvas = new NewtCanvasJFX(window);
        getChildren().add(canvas);

        // Ugly hack to fix focus
        // https://forum.jogamp.org/NewtCanvasJFX-not-giving-up-focus-td4040705.html
        canvasFocusListener = new CanvasFocusListener(canvas);
        window.addWindowListener(canvasFocusListener);

        canvas
                .focusedProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!newValue) {
                                JFXAccessor.runOnJFXThread(false, this::giveFocus);
                            }
                        });

        resizeHeightTimer = new Timer();
        resizeWidthTimer = new Timer();

        // Resize window when region resizes
        heightProperty().addListener(HEIGHT_LISTENER);
        widthProperty().addListener(WIDTH_LISTENER);

        canvas.setHeight(heightProperty().get() > 0.0 ? heightProperty().get() : getPrefHeight());
        canvas.setWidth(widthProperty().get() > 0.0 ? widthProperty().get() : getPrefWidth());

        window.addMouseListener(this);

        // Start rendering the model
        renderer = new Renderer(model, this);
        window.addGLEventListener(renderer);
        animator = new Animator(window);
        animator.setUpdateFPSFrames(10, null);
        animator.start();

        fpsUpdater =
                new Timeline(
                        new KeyFrame(Duration.millis(100), e -> fpsProperty.set(animator.getLastFPS())));
        fpsUpdater.setCycleCount(Animation.INDEFINITE);
        fpsUpdater.play();
    }

    public void setShader(Renderer.Shader shader) {
        renderer.setShader(shader);
    }

    public void dispose() {
        if (window != null) {
            window.removeMouseListener(this);

            if (renderer != null) window.removeGLEventListener(renderer);
            if (canvasFocusListener != null) window.removeWindowListener(canvasFocusListener);
        }

        heightProperty().removeListener(HEIGHT_LISTENER);
        widthProperty().removeListener(WIDTH_LISTENER);

        if (animator != null) animator.stop();

        if (fpsUpdater != null) fpsUpdater.stop();

        if (getChildren().size() > 0) {
            var newt = (NewtCanvasJFX) getChildren().get(0);
            newt.destroy();
            getChildren().remove(newt);
        }

        if (model != null) model.dispose();

        if (resizeHeightTimer != null) resizeHeightTimer.cancel();
        if (resizeWidthTimer != null) resizeWidthTimer.cancel();
    }

    public Point canvasToMap(Point point) {
        try {
            return new Point(transform.inverseTransform(point.x(), point.y()));
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public void zoom(float zoom, float x, float y) {
        if (transform.getMxx() * zoom < zoomHandler.getMaxZoom()) {
            transform.prependTranslation(-x, -y);
            transform.prependScale(
                    zoomHandler.getMaxZoom() / transform.getMxx(),
                    zoomHandler.getMaxZoom() / transform.getMxx());
            transform.prependTranslation(x, y);

        } else if (transform.getMxx() * zoom > zoomHandler.getMinZoom()) {
            transform.prependTranslation(-x, -y);
            transform.prependScale(
                    zoomHandler.getMinZoom() / transform.getMxx(),
                    zoomHandler.getMinZoom() / transform.getMxx());
            transform.prependTranslation(x, y);

        } else {
            transform.prependTranslation(-x, -y);
            transform.prependScale(zoom, zoom);
            transform.prependTranslation(x, y);
        }
    }

    public void pan(float dx, float dy) {
        transform.prependTranslation(dx, dy);
    }

    public void center(Point center) {
        transform.setTx(-center.x() * transform.getMxx() + getWidth() / 2);
        transform.setTy(-center.y() * transform.getMyy() + getHeight() / 2);
    }

    public void zoomTo(Point point) {
        setZoom(25);
        center(point);
    }

    public void zoomTo(Point point, float zoom) {
        setZoom(zoom);
        center(point);
    }

    public void zoomChange(boolean positive) {
        Point point = new Point(640, 360);
        Scale scale = new Scale(1.2, 1.2);
        if (positive) {
            zoom((float) scale.getX(), point.x(), point.y());
        } else {
            try {
                zoom((float) scale.createInverse().getX(), point.x(), point.y());
            } catch (NonInvertibleTransformException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public float getZoom() {
        return (float) (transform.getMxx() / startZoom);
    }

    public void setZoom(float zoom) {
        transform.setMxx(zoom);
        transform.setMyy(zoom);
    }

    public void setZoomHandler(Rect bounds) {
        this.zoomHandler = new ZoomHandler(bounds, this);
        startZoom = zoomHandler.getStartZoom();
        setZoom(startZoom);
    }

    public String updateZoom() {
        return zoomHandler.getZoomString();
    }

    public String updateScalebar() {
        return zoomHandler.getScaleString();
    }

    public void giveFocus() {
        if (window.isVisible()) {
            window.setVisible(false);
            window.setVisible(true);
        }
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        handle(mapMouseClickedProperty, mouseEvent);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        handle(mapMouseMovedProperty, mouseEvent);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        if (mouseEvent.getRotation()[0] == 0.0) {
            zoom(
                    (float) Math.pow(1.05, mouseEvent.getRotation()[1]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        } else {
            zoom(
                    (float) Math.pow(1.15, mouseEvent.getRotation()[0]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
        handle(mapMouseWheelProperty, mouseEvent);
    }

    private <T> void handle(ObjectProperty<EventHandler<T>> prop, T event) {
        if (prop.get() != null) {
            prop.get().handle(event);
        }
    }

    /**
     * Get the currently shown area as a lat/lon rect.
     *
     * @return Rect of lat/lon area shown by the canvas.
     */
    public Rect screenBounds() {
        return new Rect(
                Point.mapToGeo(canvasToMap(new Point(0, (float) getHeight()))),
                Point.mapToGeo(canvasToMap(new Point((float) getWidth(), 0))));
    }

    /**
     * Get all currently shown chunks.
     *
     * @return All chunks currently on screen.
     */
    public Iterable<Chunk> getChunks() {
        return model.getChunks(chunkSize()).range(screenBounds());
    }

    public float chunkSize() {
        var left = Point.mapToGeo(canvasToMap(new Point(0, 0)));
        var right = Point.mapToGeo(canvasToMap(new Point((float) getWidth(), 0)));
        return (right.x() - left.x()) / 2; // 2 is how many chunks we want on the x-axis
    }
}
