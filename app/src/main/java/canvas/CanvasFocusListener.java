package canvas;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.javafx.NewtCanvasJFX;

public class CanvasFocusListener extends WindowAdapter {
    private final NewtCanvasJFX canvas;

    public CanvasFocusListener(NewtCanvasJFX canvas) {
        this.canvas = canvas;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        // when heavyweight window gains focus, also tell javafx to give focus to glCanvas
        canvas.requestFocus();
    }
}
