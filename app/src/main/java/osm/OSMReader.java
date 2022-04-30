package osm;

import geometry.Rect;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.incubator.vector.*;
import osm.elements.*;
import osm.tables.NodeTable;
import osm.tables.WayTable;
import util.ThrowingRunnable;

public class OSMReader {
    enum Parseable {
        NODE('n', true),
        WAY('w', true),
        RELATION('r', true),
        TAG('t', false),
        ND('n', false),
        MEMBER('m', false);

        final byte b;
        final boolean isContainer;

        Parseable(char c, boolean isContainer) {
            b = (byte) c;
            this.isContainer = isContainer;
        }
    }

    private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
    private static final int SPECIES_LENGTH = SPECIES.length();

    private static final VectorMask<Byte>[] LSHIFT_MASKS =
            (VectorMask<Byte>[]) new VectorMask[SPECIES_LENGTH];
    private static final double[] POWERS_OF_TEN = new double[24];

    private static final ByteVector TAG = ByteVector.broadcast(SPECIES, (byte) '<');
    private static final ByteVector QUOTE = ByteVector.broadcast(SPECIES, (byte) '"');
    private static final ByteVector L = ByteVector.broadcast(SPECIES, (byte) 'l');
    private static final ByteVector DOT = ByteVector.broadcast(SPECIES, (byte) '.');

    static {
        for (int i = 0; i < SPECIES_LENGTH; i++) {
            LSHIFT_MASKS[i] = VectorMask.fromLong(SPECIES, 0xFFFF_FFFF_FFFF_FFFFL << i);
        }

        for (int i = 0; i < POWERS_OF_TEN.length; i++) {
            POWERS_OF_TEN[i] = Math.pow(10, i);
        }
    }

    private InputStream stream;
    private final byte[] buf = new byte[64 * 4096]; // must be multiple of 64
    private int cur = 0;
    private int offset = 0;

    private final List<OSMObserver> observers = new ArrayList<>();

    private final NodeTable nodes = new NodeTable();
    private final WayTable ways = new WayTable();

    private final OSMNode node = new OSMNode();
    private final OSMWay way = new OSMWay();
    private final OSMRelation relation = new OSMRelation();
    private OSMElement current;
    private final List<SlimOSMNode> wayNdList = new ArrayList<>();
    private boolean atTag;

    public OSMReader() {
        addObservers(nodes, ways);
    }

    public void parse(InputStream stream) throws Exception {
        this.stream =
                new SequenceInputStream(
                        stream, new ByteArrayInputStream("<end>".getBytes(StandardCharsets.UTF_8)));
        stream.read(buf);

        parseBounds();

        current = node;
        parseAll(Parseable.NODE, this::parseNode);
        current = way;
        parseAll(Parseable.WAY, this::parseWay);
        current = relation;
        parseAll(Parseable.RELATION, this::parseRelation);

        for (var observer : observers) {
            observer.onFinish();
        }
    }

    public void addObservers(OSMObserver... observers) {
        this.observers.addAll(Arrays.asList(observers));
    }

    private void refill() throws IOException {
        int half = buf.length >> 1;

        if (cur >= half) {
            System.arraycopy(buf, half, buf, 0, half);
            stream.read(buf, half, half);
            cur -= half;
        }
    }

    private byte read() {
        advance();
        return at();
    }

    private byte at() {
        return buf[cur + offset];
    }

    private void advance() {
        atTag = false;
        offset++;

        if (offset == SPECIES_LENGTH) {
            cur += SPECIES_LENGTH;
            offset = 0;
        }
    }

    private void advance(int amount) {
        atTag = false;
        offset += amount;

        while (offset >= SPECIES_LENGTH) {
            cur += SPECIES_LENGTH;
            offset -= SPECIES_LENGTH;
        }
    }

    private void advance(ByteVector until) {
        offset =
                ByteVector.fromArray(SPECIES, buf, cur).eq(until).and(LSHIFT_MASKS[offset]).firstTrue();

        // The happy path would be avoiding this condition.
        if (offset == SPECIES_LENGTH) {
            cur += SPECIES_LENGTH;
            advanceLoop(until);
        }
    }

    private void advanceLoop(ByteVector until) {
        while (true) {
            offset = ByteVector.fromArray(SPECIES, buf, cur).eq(until).firstTrue();

            if (offset == SPECIES_LENGTH) {
                cur += SPECIES_LENGTH;
            } else {
                return;
            }
        }
    }

    private void advanceTag() {
        advance(TAG);
        advance();
        atTag = true;
    }

    private void parseBounds() {
        do {
            advanceTag();
        } while (at() != 'b');

        double minlat;
        double minlon;
        double maxlat;
        double maxlon;

        advance(L);
        var latFirst = read() == 'a';
        advance(QUOTE);
        if (latFirst) {
            minlat = getDouble();
            advance(QUOTE);
            minlon = getDouble();
        } else {
            minlon = getDouble();
            advance(QUOTE);
            minlat = getDouble();
        }

        advance(L);
        latFirst = read() == 'a';
        advance(QUOTE);
        if (latFirst) {
            maxlat = getDouble();
            advance(QUOTE);
            maxlon = getDouble();
        } else {
            maxlon = getDouble();
            advance(QUOTE);
            maxlat = getDouble();
        }

        var bounds = new Rect((float) minlat, (float) minlon, (float) maxlat, (float) maxlon);

        for (var observer : observers) {
            observer.onBounds(bounds);
        }
    }

    private void parseAll(Parseable parseable, ThrowingRunnable runnable) throws Exception {
        while (true) {
            refill();

            if (!atTag) advanceTag();

            if (at() != parseable.b) {
                if (!parseable.isContainer || at() != '/') {
                    return;
                }

                advanceTag();
            } else {
                runnable.run();
            }
        }
    }

    private void parseNode() throws Exception {
        advance(8); // advance(QUOTE);
        var id = getLong();

        advance(L);
        advance(4); // advance(QUOTE);
        var lat = getDouble();
        advance(L);
        advance(4); // advance(QUOTE);
        var lon = getDouble();

        node.init(id, lon, lat);

        parseAll(Parseable.TAG, this::parseTag);

        for (var observer : observers) {
            observer.onNode(node);
        }
    }

    private void parseWay() throws Exception {
        advance(7); // advance(QUOTE);
        var id = getLong();

        way.init(id);

        parseAll(Parseable.ND, this::parseNd);
        parseAll(Parseable.TAG, this::parseTag);

        way.setNodes(wayNdList.toArray(new SlimOSMNode[0]));
        wayNdList.clear();

        for (var observer : observers) {
            observer.onWay(way);
        }
    }

    private void parseRelation() throws Exception {
        advance(12); // advance(QUOTE);
        var id = getLong();

        relation.init(id);

        parseAll(Parseable.MEMBER, this::parseMember);
        parseAll(Parseable.TAG, this::parseTag);

        for (var observer : observers) {
            observer.onRelation(relation);
        }
    }

    private void parseTag() {
        advance(6); // advance(QUOTE);
        var k = getString();
        var key = OSMTag.Key.from(k);
        if (key == null) return;

        advance(3); // advance(QUOTE);
        var v = getString().intern();
        var tag = new OSMTag(key, v);

        current.tags().add(tag);
    }

    private void parseNd() {
        advance(7); // advance(QUOTE);
        var ref = getLong();
        var node = nodes.get(ref);
        if (node == null) return;
        wayNdList.add(node);
    }

    private void parseMember() {
        advance(12); // advance(QUOTE);
        var type = read();

        if (type != 'w') return; // way

        advance(9); // advance(QUOTE);
        var ref = getLong();

        advance(6); // advance(QUOTE);
        var role = read();

        if (role != 'o') return; // outer

        var way = ways.get(ref);
        if (way == null) return;

        ((OSMRelation) current).ways().add(way);
    }

    private String getString() {
        advance();
        int off = cur + offset;
        advance();
        advance(QUOTE);
        int len = cur + offset - off;

        advance(); // advance past end quote

        return new String(buf, off, len, StandardCharsets.UTF_8);
    }

    private long getLong() {
        long num = 0;

        advance();
        int off = cur + offset;
        advance();
        advance(QUOTE);
        int len = cur + offset - off;

        for (int i = 0; i < len; i++) {
            num = num * 10 + buf[off + i] - '0';
        }

        advance(); // advance past end quote

        return num;
    }

    private double getDouble() {
        int off = cur + offset + 1;
        advance(DOT);
        int len = cur + offset - off;

        // really nasty edge-case where some coords don't have any decimal places
        if (len > 4) {
            return getDoubleNoDecimals(off);
        }

        int first = readInt(off, len);

        off = cur + offset + 1;
        advance(QUOTE);
        len = cur + offset - off;

        int second = readInt(off, len);

        advance(); // advance past end quote

        return first + second / POWERS_OF_TEN[len];
    }

    private double getDoubleNoDecimals(int start) {
        // backtrack
        cur = (start - 1) / SPECIES_LENGTH * SPECIES_LENGTH;
        offset = start - cur;

        // parse again
        int off = cur + offset;
        advance(QUOTE);
        int len = cur + offset - off;

        return readInt(off, len);
    }

    private int readInt(int off, int len) {
        int num = 0;
        for (int i = 0; i < len; i++) {
            num = num * 10 + buf[off + i] - '0';
        }
        return num;
    }
}
