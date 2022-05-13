package navigation;

import geometry.Point;
import java.util.List;
import util.DistanceUtils;

public class Instructions {
    private final List<Road> edges;
    private double preDir = 0.0;
    private double preAbs;
    private Road preEdge;
    private Road currentEdge;
    private Road nextEdge;
    private double lastDistance = 0.0;
    private String name = "Unnamed way";
    private String instructionsString = "";
    private RoadRole role;

    public Instructions(List<Road> edges) {
        this.edges = edges;
        getInstructions();
    }

    private void getInstructions() {
        for (int i = 0; i < edges.size(); i++) {
            // Checks if it is the first edge
            if (preEdge == null) {
                preEdge = edges.get(0);
                nextEdge = edges.get(i + 1);
                if (!preEdge.name().equals("Unnamed way")) {
                    instructionsString += "Start at " + preEdge.name() + "\n";
                }
                continue;
            }
            double calc = calc(edges.get(i));
            preDir = calc(preEdge);
            currentEdge = edges.get(i);
            // Checks if it is the last edge
            if (i < edges.size() - 1) {
                nextEdge = edges.get(i + 1);
            } else {
                instructionsString +=
                        "Stay at "
                                + edges.get(i).name()
                                + " for "
                                + getDistance(currentEdge.from(), currentEdge.to())
                                + "\n";
                break;
            }
            getInstructionString(alignOrientation(preDir, calc) - preDir);
            preEdge = edges.get(i);
        }
        instructionsString += "You have arrived" + "\n";
    }

    private void getInstructionString(double calc) {
        double abs = Math.abs(calc);
        String dist = getDistance(currentEdge.from(), currentEdge.to());

        if (!preEdge.name().equals("Unnamed way")) {
            name = preEdge.name();
            role = preEdge.role();
        } else if (preEdge.name().equals("Unnamed way")
                && name.equals("Unnamed way")
                && !currentEdge.name().equals("Unnamed way")) {
            name = currentEdge.name();
        }
        switch (currentEdge.role()) {
            case MOTORWAY:
                if (!preEdge.name().equals(currentEdge.name())
                        && !currentEdge.name().equals("Unnamed way")
                        && !nextEdge.name().equals("Unnamed way")
                        && preEdge.role() != RoadRole.MOTORWAYLINK) {
                    instructionsString +=
                            "Stay at "
                                    + name
                                    + " for "
                                    + dist
                                    + "and then continue on "
                                    + currentEdge.name()
                                    + "\n";
                    lastDistance = 0.0;
                } else if (!preEdge.name().equals(currentEdge.name())
                        && !currentEdge.name().equals("Unnamed way")
                        && !nextEdge.name().equals("Unnamed way")
                        && preEdge.role() == RoadRole.MOTORWAYLINK) {
                    instructionsString += "Merge with " + currentEdge.name() + " in " + dist + "\n";
                    lastDistance = 0.0;
                }
                break;

            case MOTORWAYLINK:
                switch (nextEdge.role()) {
                    case WAY:
                        // Driving from way to motorway
                        instructionsString +=
                                "Stay at "
                                        + name
                                        + " and in "
                                        + dist
                                        + "then take the exit towards "
                                        + nextEdge.name()
                                        + "\n";
                        lastDistance = 0.0;
                        break;
                    case MOTORWAY:
                        if (name.equals(nextEdge.name())) {
                            break;
                        }
                        // Checks if it is a link between two motorways
                        else if (role == RoadRole.MOTORWAY) {
                            instructionsString +=
                                    "Stay at " + name + " for " + dist + "then continue on " + nextEdge.name() + "\n";
                        } else {
                            instructionsString +=
                                    "Stay at "
                                            + name
                                            + " and then take the access road in "
                                            + dist
                                            + "to "
                                            + nextEdge.name()
                                            + "\n";
                        }
                        lastDistance = 0.0;
                        break;
                    default:
                        break;
                }
                break;

            case ROUNDABOUT:
                if (preEdge.role() != RoadRole.ROUNDABOUT) {
                    instructionsString +=
                            "Stay at " + name + " and then take the roundabout in " + dist + "\n";
                    lastDistance = 0.0;
                } else if (nextEdge.role() != RoadRole.ROUNDABOUT) {
                    instructionsString += "Exit the roundabout towards " + nextEdge.name() + "\n";
                    lastDistance = 0.0;
                }
                break;

            default:
                // If it is an unnamed way we add the orientation so we get the turn correct
                if (preEdge.name().equals("Unnamed way")
                        && currentEdge.name().equals("Unnamed way")
                        && nextEdge.name().equals("Unnamed way")) {
                    preAbs += abs;
                } else if (preAbs > abs && preAbs > 0.7 && !currentEdge.name().equals("Unnamed way")) {
                    abs = preAbs;
                    preAbs = 0;
                }
                if (abs > 0.7
                        && preEdge.role() != RoadRole.ROUNDABOUT
                        && !currentEdge.name().equals("Unnamed way")) {
                    if (calc > 0) {
                        instructionsString +=
                                "Stay at "
                                        + name
                                        + " and then turn left in "
                                        + dist
                                        + "at "
                                        + currentEdge.name()
                                        + " \n";
                    } else {
                        instructionsString +=
                                "Stay at "
                                        + name
                                        + " and then turn right in "
                                        + dist
                                        + "at "
                                        + currentEdge.name()
                                        + "\n";
                    }
                    lastDistance = 0.0;
                }
                // Exiting a motorway and turning
                else if (preEdge.role() == RoadRole.MOTORWAYLINK
                        && currentEdge.role() != RoadRole.MOTORWAYLINK
                        && currentEdge.role() != RoadRole.ROUNDABOUT) {
                    if (calc > 0) {
                        instructionsString += "Turn left in " + dist + "at " + currentEdge.name() + "\n";
                    } else {
                        instructionsString += "Turn right in " + dist + "at " + currentEdge.name() + "\n";
                    }
                    lastDistance = 0.0;
                } else if (preEdge.role() == RoadRole.LINK && currentEdge.role() != RoadRole.LINK) {
                    if (calc > 0) {
                        instructionsString +=
                                "Stay at "
                                        + name
                                        + " and then turn left in "
                                        + dist
                                        + "at "
                                        + currentEdge.name()
                                        + "\n";
                    } else {
                        instructionsString +=
                                "Stay at "
                                        + name
                                        + " and then turn right in "
                                        + dist
                                        + "at "
                                        + currentEdge.name()
                                        + "\n";
                    }
                    lastDistance = 0.0;
                }
                // Checks if the name of the street changes
                else if (!preEdge.name().equals(currentEdge.name())
                        && !preEdge.name().equals("Unnamed way")
                        && !currentEdge.name().equals("Unnamed way")
                        && preEdge.role() != RoadRole.ROUNDABOUT) {
                    instructionsString +=
                            "Stay at "
                                    + name
                                    + " for "
                                    + dist
                                    + "and then continue on "
                                    + currentEdge.name()
                                    + "\n";
                    lastDistance = 0.0;
                }
                break;
        }
    }

    // Calculates the angle in radians
    private double calc(Road edge) {
        double fromLat = edge.from().y();
        double fromLon = edge.from().x();
        double toLat = edge.to().y();
        double toLon = edge.to().x();
        return (Math.atan2(toLat - fromLat, (toLon - fromLon)));
    }

    private double alignOrientation(double baseOrientation, double orientation) {
        double resultOrientation;
        if (baseOrientation >= 0) {
            if (orientation < -Math.PI + baseOrientation) {
                resultOrientation = orientation + (2 * Math.PI);
            } else {
                resultOrientation = orientation;
            }
        } else if (orientation > +Math.PI + baseOrientation) {
            resultOrientation = orientation - (2 * Math.PI);
        } else {
            resultOrientation = orientation;
        }
        return resultOrientation;
    }

    private String getDistance(Point from, Point to) {
        double dist = (DistanceUtils.calculateEarthDistance(from, to) * 1000) + lastDistance;
        lastDistance = dist;
        if (dist < 1000) {
            int distM = (int) Math.round(dist / 10.0) * 10;
            return distM + " m ";
        } else {
            dist = (Math.round((dist / 1000) * 100.0) / 100.0);
            return dist + " km ";
        }
    }

    public String getInstructionsString() {
        return instructionsString;
    }
}
