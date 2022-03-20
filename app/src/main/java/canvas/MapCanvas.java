package canvas;

import com.jogamp.nativewindow.javafx.JFXAccessor;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import java.nio.FloatBuffer;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;

public class MapCanvas extends Region {
    private final Affine transform = new Affine();
    private Animator animator;
    private FloatBuffer transformBuffer;
    private GLWindow window;

    public void init(Model model) {
        recalculateTransform();

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
                                            window.setVisible(false);
                                            window.setVisible(true);
                                        });
                            }
                        });

        // Resize window when region resizes
        widthProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(newValue.intValue(), window.getHeight()));
        heightProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(window.getWidth(), newValue.intValue()));

        canvas.setWidth(getPrefWidth());
        canvas.setHeight(getPrefHeight());

        // Start rendering the model
        window.addGLEventListener(new Renderer(model, this));
        animator = new Animator(window);
        animator.start();
    }

    public void dispose() {
        animator.stop();
    }

    public void addMouseListener(MouseListener mouseListener) {
        window.addMouseListener(mouseListener);
    }

    public FloatBuffer getTransformBuffer() {
        return transformBuffer;
    }

    public void zoom(float zoom, float x, float y) {
        transform.prependTranslation(-x, y);
        transform.prependScale(zoom, zoom);
        transform.prependTranslation(x, -y);
        recalculateTransform();
    }

    public void pan(float dx, float dy) {
        transform.prependTranslation(dx, -dy);
        recalculateTransform();
    }

    private void recalculateTransform() {
        // Extract column major 4x4 matrix from Affine to buffer
        transformBuffer =
                FloatBuffer.allocate(16)
                        .put((float) transform.getMxx())
                        .put((float) transform.getMyx())
                        .put(0)
                        .put(0)
                        .put((float) transform.getMxy())
                        .put((float) transform.getMyy())
                        .put(0)
                        .put(0)
                        .put(0)
                        .put(0)
                        .put(1)
                        .put(0)
                        .put((float) transform.getTx())
                        .put((float) transform.getTy())
                        .put(0)
                        .put(1);
    }
}
