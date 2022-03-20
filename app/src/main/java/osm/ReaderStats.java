package osm;

import osm.elements.OSMNode;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

/** Simple observer that just prints out some stats every second */
public class ReaderStats implements OSMObserver {
    private long prevNodeCount = 0;
    private long prevWayCount = 0;
    private long prevRelationCount = 0;
    private long curNodeCount = 0;
    private long curWayCount = 0;
    private long curRelationCount = 0;

    private long lastUpdate = System.nanoTime();

    private void checkUpdate() {
        if (System.nanoTime() - lastUpdate < 1_000_000_000) return; // Every second
        lastUpdate = System.nanoTime();

        System.out.printf(
                "\r%d nodes (%d nodes/s) : %d ways (%d ways/s) : %d relations (%d relations/s)",
                curNodeCount,
                curNodeCount - prevNodeCount,
                curWayCount,
                curWayCount - prevWayCount,
                curRelationCount,
                curRelationCount - prevRelationCount);
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
        System.out.println("\rParsed: " + curNodeCount + " nodes");
        System.out.println("Parsed: " + curWayCount + " ways");
        System.out.println("Parsed: " + curRelationCount + " relations");
    }
}
