package drawing;

import collections.Entity;
import geometry.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class DrawingManager {
    private final List<DrawingInfo> drawings = new ArrayList<>();
    private final Drawing drawing = new Drawing();

    private DrawingInfo createDrawingInfo(Drawing drawing) {
        return new DrawingInfo(
                drawing.offset(this.drawing.vertices().size() / 2),
                this.drawing.indices().size(),
                this.drawing.vertices().size(),
                this.drawing.drawables().size());
    }

    /**
     * Helper method that creates a drawing, adds it to the manager, and returns a reference to the
     * newly created drawing.
     *
     * @param points
     * @param drawable
     * @return
     */
    public DrawingInfo draw(List<Vector2D> points, Drawable drawable) {
        return draw(points, drawable, 0);
    }

    /**
     * Helper method that creates a drawing, adds it to the manager, and returns a reference to the
     * newly created drawing.
     *
     * @param points
     * @param drawable
     * @return
     */
    public DrawingInfo draw(List<Vector2D> points, Drawable drawable, int offset) {
        return draw(Drawing.create(points, drawable, offset));
    }

    public DrawingInfo draw(Vector2D point, Drawable drawable) {
        return draw(Drawing.create(point, drawable));
    }

    /**
     * Add a drawing to the manager.
     *
     * @param drawing Drawing to add.
     */
    public DrawingInfo draw(Drawing drawing) {
        var info = createDrawingInfo(drawing);
        drawings.add(info);
        this.drawing.draw(drawing);
        return info;
    }

    /**
     * Remove all drawings, but retain allocated memory.
     */
    public void clear() {
        drawings.clear();
        drawing.clear();
    }

    /**
     * Clear a single drawing from the manager. This operation takes linear time in the worst case and
     * constant time if the drawing being removed was the latest added. One should prefer to clear
     * drawings in a LIFO order.
     *
     * @param drawing The Drawing to clear.
     * @return Info after which the changes have been made.
     */
    public DrawingInfo clear(Entity drawing) {
        // Search for entity
        var idx = -1;
        for (int i = 0; i < drawings.size(); i++) {
            if (drawings.get(i).drawing.equals(drawing)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return null;

        // Reset state to how it was before the drawing was added
        var info = drawings.get(idx);
        this.drawing.indices().limit(info.indicesStart());
        this.drawing.vertices().limit(info.verticesStart());
        this.drawing.drawables().limit(info.drawablesStart());

        // Shift every drawing to the right of idx left by 1, rewriting history as if the drawing was
        // never there
        var infoDrawing = new Drawing();
        for (int i = idx; i < drawings.size() - 1; i++) {
            var curInfo = drawings.get(i + 1);
            var d = curInfo.drawing().offset(-curInfo.verticesStart() / 2);
            drawings.set(i, createDrawingInfo(d));
            infoDrawing.draw(d);
            this.drawing.draw(d);
        }
        drawings.remove(drawings.size() - 1);

        return new DrawingInfo(
                infoDrawing.offset(info.verticesStart() / 2),
                info.indicesStart(),
                info.verticesStart(),
                info.drawablesStart());
    }

    public int byteSize() {
        return drawing.byteSize();
    }

    /**
     * Reference to underlying drawing. Do not mutate.
     *
     * @return
     */
    public Drawing drawing() {
        return drawing;
    }

    public record DrawingInfo(
            Drawing drawing, int indicesStart, int verticesStart, int drawablesStart) {
    }
}
