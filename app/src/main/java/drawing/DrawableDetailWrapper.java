package drawing;

import javafx.scene.paint.Color;

public class DrawableDetailWrapper implements Drawable {
    private static final DrawableDetailWrapper INSTANCE = new DrawableDetailWrapper();

    private Drawable drawable;
    private double size;

    private DrawableDetailWrapper() {
    }

    /**
     * Get a Drawable with a new size. To avoid allocating, this method modifies and returns the same
     * instance with every call.
     *
     * @param drawable Drawable to wrap
     * @param detail   Detail from which to calculate new size
     * @return Wrapped drawable with new size
     */
    public static Drawable from(Drawable drawable, int detail) {
        INSTANCE.drawable = drawable;
        INSTANCE.size = drawable.size() * Math.pow(4, detail);
        return INSTANCE;
    }

    @Override
    public Shape shape() {
        return drawable.shape();
    }

    @Override
    public Color color() {
        return drawable.color();
    }

    @Override
    public double size() {
        return size;
    }

    @Override
    public Category category() {
        return drawable.category();
    }

    @Override
    public int ordinal() {
        return drawable.ordinal();
    }

    @Override
    public int detail() {
        return drawable.detail();
    }
}
