package drawing;

import javafx.scene.paint.Color;

public interface Drawable {
    Shape shape();

    Color color();

    double size();

    Category category();

    int ordinal();

    int detail();

    enum Shape {
        POLYLINE,
        FILL
    }

    enum Category {
        ROAD,
        LAND,
        CITY,
        COAST,
        MISC,
        DEBUG
    }
}
