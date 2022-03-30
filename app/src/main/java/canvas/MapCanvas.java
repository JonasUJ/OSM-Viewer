package canvas;

import collections.enumflags.ObservableEnumFlags;
import com.jogamp.nativewindow.javafx.JFXAccessor;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import drawing.Category;
import geometry.Point;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Region {
    final Affine transform = new Affine();
    private Animator animator;
    private GLWindow window;
    private Renderer renderer;

    public final ObservableEnumFlags<Category> categories = new ObservableEnumFlags<>();

    public void init(Model model) {
        // Boilerplate to let us use OpenGL from a JavaFX node hierarchy
        Platform.setImplicitExit(true);
        window = GLWindow.create(model.getCaps());
        window.setSharedAutoDrawable(model.getSharedDrawable());
        final var canvas = new NewtCanvasJFX(window);
        getChildren().add(canvas);

        // Ugly hack to fix focus
        // https://forum.jogamp.org/NewtCanvasJFX-not-giving-up-focus-td4040705.html
        window.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        // when heavyweight window gains focus, also tell javafx to give focus to glCanvas
                        canvas.requestFocus();
                    }
                });

        canvas
                .focusedProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!newValue) {
                                JFXAccessor.runOnJFXThread(
                                        false,
                                        () -> {
                                            if (window.isVisible()) {
                                                window.setVisible(false);
                                                window.setVisible(true);
                                            }
                                        });
                            }
                        });

        // Resize window when region resizes
        widthProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(Math.max(1, newValue.intValue()), window.getHeight()));
        heightProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(window.getWidth(), Math.max(1, newValue.intValue())));

        canvas.setWidth(getPrefWidth());
        canvas.setHeight(getPrefHeight());

        // Start rendering the model
        renderer = new Renderer(model, this);
        window.addGLEventListener(renderer);
        animator = new Animator(window);
        animator.start();
    }

    public void setShader(Renderer.Shader shader) {
        renderer.setShader(shader);
    }

    public void dispose() {
        animator.stop();
    }

    public void addMouseListener(MouseListener mouseListener) {
        window.addMouseListener(mouseListener);
    }

    public Point canvasToMap(Point point) {
        try {
            return new Point(transform.inverseTransform(point.x(), point.y()));
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public void zoom(float zoom, float x, float y) {
        transform.prependTranslation(-x, -y);
        transform.prependScale(zoom, zoom);
        transform.prependTranslation(x, y);
    }

    public void pan(float dx, float dy) {
        transform.prependTranslation(dx, dy);
    }

    public void center(Point center) {
        transform.setTx(-center.x() * transform.getMxx() + getWidth() / 2);
        transform.setTy(-center.y() * transform.getMyy() + getHeight() / 2);

    }

    public void setZoom(float zoom){
        transform.setMxx(zoom);
        transform.setMyy(zoom);

    }
}
