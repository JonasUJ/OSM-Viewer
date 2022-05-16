package canvas;

import geometry.Point;
import geometry.Rect;

public class ZoomHandler {

    private static final float R = 6371; // Earth radius in km
    private final Rect bounds;
    private final MapCanvas canvas;
    private final float currentScale;
    private final float max;
    private final boolean isX;

    public ZoomHandler(Rect bounds, MapCanvas canvas) {
        this.bounds = bounds;
        this.canvas = canvas;
        currentScale =
                (float)
                        (getDistance(
                                bounds.getBottomLeft().x(),
                                bounds.getBottomLeft().y(),
                                bounds.getBottomRight().x(),
                                bounds.getBottomRight().y())
                                * (100 / canvas.getPrefWidth()));
        if (1280
                / (Point.geoToMap(bounds.getBottomRight()).x()
                - Point.geoToMap(bounds.getTopLeft()).x())
                > 720
                / (Point.geoToMap(bounds.getTopLeft()).y()
                - Point.geoToMap(bounds.getBottomRight()).y())) {
            max = (Point.geoToMap(bounds.getTopLeft()).y() - Point.geoToMap(bounds.getBottomRight()).y());
            isX = false;
        } else {
            max = (Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x());
            isX = true;
        }
    }

    public static float getDistance(float lon1, float lat1, float lon2, float lat2) {
        // Haversine method
        var deltaLat = degreeToRadian(lat2 - lat1);
        var deltaLon = degreeToRadian(lon2 - lon1);
        var a =
                Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                        + Math.cos(degreeToRadian(lat1))
                        * Math.cos(degreeToRadian(lat2))
                        * Math.sin(deltaLon / 2)
                        * Math.sin(deltaLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c;
        return (float) (d * 1000); // convert to metres
    }

    private static float degreeToRadian(float degree) {
        return (float) (degree * (Math.PI / 180));
    }

    public String getZoomString() {
        float zoom;
        if (isX) {
            zoom =
                    (20 / max
                            + 1
                            - ((canvas.canvasToMap(new Point(1280, 0)).x()
                            - canvas.canvasToMap(new Point(0, 0)).x())
                            / max))
                            * 100;
        } else {
            zoom =
                    (20 / max
                            + 1
                            - ((canvas.canvasToMap(new Point(0, 720)).y()
                            - canvas.canvasToMap(new Point(0, 0)).y())
                            / max))
                            * 100;
        }
        return (float) (Math.round((zoom) * 10.0) / 10.0) + "%";
    }

    public float getMaxZoom() {
        if (isX) {
            return 1280 / (max + 20);
        } else {
            return 720 / (max + 20);
        }
    }

    public float getMinZoom() {
        if (isX) {
            return 64; // 1280/20
        } else {
            return 36; // 720/20
        }
    }

    public String getScaleString() {
        var newScale = currentScale * (1 / canvas.getZoom());
        if (newScale < 1000) {
            return (float) (Math.round(newScale * 100.0) / 100.0) + "m";
        } else {
            return (float) (Math.round((newScale / 1000) * 100.0) / 100.0) + "km";
        }
    }

    public float getStartZoom() {
        return (1280
                / (Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x()));

    }
}
