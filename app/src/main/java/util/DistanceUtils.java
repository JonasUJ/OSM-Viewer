package util;

import geometry.Point;

public abstract class DistanceUtils {
    private static final int R = 6371; // Earth radius in km

    public static double calculateEarthDistance(Point from, Point to) {
        var lon1 = from.x();
        var lat1 = from.y();
        var lon2 = to.x();
        var lat2 = to.y();

        // Haversine formula
        var deltaLat = degreeToRadian(lat2 - lat1);
        var deltaLon = degreeToRadian(lon2 - lon1);
        var a =
                Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                        + Math.cos(degreeToRadian(lat1))
                        * Math.cos(degreeToRadian(lat2))
                        * Math.sin(deltaLon / 2)
                        * Math.sin(deltaLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public static double degreeToRadian(double degree) {
        return degree * (Math.PI / 180);
    }
}
