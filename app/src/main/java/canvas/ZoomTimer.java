package canvas;

import geometry.Point;
import javafx.scene.transform.Affine;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ZoomTimer extends TimerTask {

    Affine transform;
    float finalZoom;
    float zoomIncrements;
    Point finalCenter;
    float panIncrements;
    Timer timer;
    MapCanvas canvas;
    float xPanIncrements, yPanIncrements;
    float frames;
    float panFrames;
    float zoomFrames;

    float zoomFactor;
    float startZoomFactor;
    boolean quickZoom = false;


    public ZoomTimer(MapCanvas canvas, Point center, float zoom, Timer timer){

        this.canvas=canvas;
        transform=canvas.transform;

        finalZoom=zoom;
        finalCenter=center;

        zoomFrames = 40;
        panFrames = 40;

        var distance=zoom-transform.getMxx();
        var xPanDistance=center.x()-canvas.getCenterPoint().x();
        var yPanDistance=center.y()-canvas.getCenterPoint().y();

        zoomIncrements= (float) distance/zoomFrames;
        xPanIncrements=xPanDistance/panFrames;
        yPanIncrements=yPanDistance/panFrames;
        this.timer=timer;

        frames=0;
        startZoomFactor=0f;
        zoomFactor = startZoomFactor;

    }

    @Override
    public void run() {

        var startPoint=canvas.getCenterPoint();
        if (frames<zoomFrames){
            transform.setMxx(transform.getMxx()+zoomIncrements*zoomFactor);
            transform.setMyy(transform.getMyy()+zoomIncrements*zoomFactor);
            if (!quickZoom){
                zoomFactor+= ((1+(1-startZoomFactor)) -startZoomFactor) / zoomFrames;
            }

        }

        if (frames<panFrames){
            var point =new Point(startPoint.x()+xPanIncrements,startPoint.y()+yPanIncrements);
            canvas.center(point);

        }else{

            if (!quickZoom){
                var missingFrames=zoomFrames-frames;
                frames=zoomFrames-15;
                zoomIncrements*=missingFrames/15;
                quickZoom=true;
            }

            canvas.center(finalCenter);
        }


        frames++;
        if (frames>=zoomFrames && frames>=panFrames){
           // canvas.setZoom(finalZoom);
            canvas.center(finalCenter);
            timer.cancel();
        }
    }


}
