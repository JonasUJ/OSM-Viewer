package canvas;

import geometry.Point;
import geometry.Rect;

public class ZoomHandler {
    private Rect bounds;
    private float startZoom;
    private float startZoomPercentage;
    private MapCanvas canvas;
    private float currentScale;
    private final float R = 6371; //Earth radius in km
    private String zoomLevelText;
    private String scaleBarText;
    private float max;

    public ZoomHandler(Rect bounds, MapCanvas canvas){
        this.bounds = bounds;
        this.canvas = canvas;
        currentScale = (float) (getScaleBarDistance() * (100/canvas.getPrefWidth()));
        startZoom = (float) (1280/(Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x()));
        startZoomPercentage = (float) ((720/(Point.geoToMap(bounds.getTopLeft()).y()- Point.geoToMap(bounds.getBottomRight()).y()))/startZoom);
    }
    private boolean first = true;
    private boolean isX = true;
    public String getZoomString(){
        if (first){
            if (Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x() < Point.geoToMap(bounds.getTopLeft()).y() - Point.geoToMap(bounds.getBottomRight()).y()){
                max = Point.geoToMap(bounds.getTopLeft()).y() - Point.geoToMap(bounds.getBottomRight()).y();
                isX = false;
            }else {
                max = Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x();
                isX = true;
            }
            first = false;
        }
        float zoom;
        if (isX){
            zoom = max/(canvas.canvasToMap(new Point(1280, 0)).x() - canvas.canvasToMap(new Point(0, 0)).x());
        }else {
            zoom = max/(canvas.canvasToMap(new Point(0, 720)).y() - canvas.canvasToMap(new Point(0, 0)).y());
        }
        float zoomToPercentage = (zoom-1)*100;
        zoomLevelText = Float.toString((float) (Math.round(zoomToPercentage*10.0)/10.0)) + "%";
        return zoomLevelText;
    }
        
   public String getScaleString(){
        var newScale = (float) (currentScale *  (1/canvas.getZoom()));
        if (newScale < 1000){
            scaleBarText = Float.toString((float) (Math.round(newScale*100.0)/100.0)) + "m";
        } else {
            scaleBarText = Float.toString((float) (Math.round((newScale/1000)*100.0)/100.0)) + "km";
        } 
        return scaleBarText;
   }


    public float getStartZoom(){
        if (startZoomPercentage < 1) {
            return startZoomPercentage;
        } else {
            return 1;
        }
    } 
    private float getScaleBarDistance(){
        float lon1 = bounds.getBottomLeft().x();
        float lat1 = bounds.getBottomLeft().y();
        float lon2 = bounds.getBottomRight().x();
        float lat2 = bounds.getBottomRight().y();
        //Haversine method
        var deltaLat = degreeToRadian(lat2-lat1);
        var deltaLon = degreeToRadian(lon2-lon1);
        var a = 
            Math.sin(deltaLat/2) * Math.sin(deltaLat/2) + 
            Math.cos(degreeToRadian(lat1)) * Math.cos(degreeToRadian(lat2))*
            Math.sin(deltaLon/2) * Math.sin(deltaLon/2)
            ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c;
        return (float) (d * 1000); //convert to metres
    }

    private float degreeToRadian(float degree){
        return (float) (degree * (Math.PI/180));
    }
}
