package canvas;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;

public class Controller implements MouseListener {
    private Point2D lastMouse;

    @FXML private MapCanvas canvas;

    public void init(Model model) {
        canvas.init(model);
        canvas.addMouseListener(this);
    }

    public void dispose() {
        canvas.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

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
    public void mouseMoved(MouseEvent mouseEvent) {}

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        canvas.pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        canvas.zoom(
                (float) Math.pow(1.05, mouseEvent.getRotation()[1]), mouseEvent.getX(), mouseEvent.getY());
    }
}
