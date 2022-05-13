package osm;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import osm.elements.OSMNode;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

/**
 * Simple observer that just prints out some stats every second
 */
public class ReaderStats implements OSMObserver {
    public final LongProperty nodeTotal = new SimpleLongProperty();
    public final LongProperty wayTotal = new SimpleLongProperty();
    public final LongProperty relationTotal = new SimpleLongProperty();
    public final LongProperty nodeThroughput = new SimpleLongProperty();
    public final LongProperty wayThroughput = new SimpleLongProperty();
    public final LongProperty relationThroughput = new SimpleLongProperty();
    private final long updateInterval;
    private long prevNodeCount = 0;
    private long prevWayCount = 0;
    private long prevRelationCount = 0;
    private long curNodeCount = 0;
    private long curWayCount = 0;
    private long curRelationCount = 0;
    private long lastUpdate = System.nanoTime();

    public ReaderStats(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    private void checkUpdate() {
        if (System.nanoTime() - lastUpdate < updateInterval) return; // Every second
        update();
    }

    private void update() {
        lastUpdate = System.nanoTime();

        long nodeTP = curNodeCount - prevNodeCount;
        long wayTP = curWayCount - prevWayCount;
        long relationTP = curRelationCount - prevRelationCount;

        Platform.runLater(
                () -> {
                    nodeTotal.set(curNodeCount);
                    wayTotal.set(curWayCount);
                    relationTotal.set(curRelationCount);
                    nodeThroughput.set(nodeTP);
                    wayThroughput.set(wayTP);
                    relationThroughput.set(relationTP);
                });

        prevNodeCount = curNodeCount;
        prevWayCount = curWayCount;
        prevRelationCount = curRelationCount;
    }

    @Override
    public void onNode(OSMNode node) {
        curNodeCount++;
        checkUpdate();
    }

    @Override
    public void onWay(OSMWay way) {
        curWayCount++;
        checkUpdate();
    }

    @Override
    public void onRelation(OSMRelation relation) {
        curRelationCount++;
        checkUpdate();
    }

    @Override
    public void onFinish() {
        prevNodeCount = curNodeCount;
        prevWayCount = curWayCount;
        prevRelationCount = curRelationCount;
        update();
    }
}
