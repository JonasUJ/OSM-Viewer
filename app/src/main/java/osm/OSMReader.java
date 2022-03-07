package osm;

import collections.*;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class OSMReader {
    enum Parseable {
        node,
        way,
        relation,
        tag,
        nd,
        member
    }

    final XMLStreamReader reader;

    // `i` is tag key and `i + 1` is tag value
    final ArrayList<String> tags = new ArrayList<>();

    // Maps from a node ref to an index for the below lists
    final RefTable nodeRefs = new RefTable();
    // `i` is lon and `i + 1` is lat
    final DoubleList nodeCoords = new DoubleList();
    // `i` is tag start index and `i + 1` is end index
    final IntList nodeTags = new IntList();

    // Maps from a way ref to an index for the below lists
    final RefTable wayRefs = new RefTable();
    // Contains lists of node indices
    final ArrayList<IntList> wayNodes = new ArrayList<>();
    // `i` is tag start index and `i + 1` is end index
    final IntList wayTags = new IntList();
    // Tag indices for way tags with drawing information
    final IntList wayDrawables = new IntList();

    // Contains lists of way indices
    final ArrayList<IntList> relationWays = new ArrayList<>();
    // `i` is tag start index and `i + 1` is end index
    final IntList relationTags = new IntList();

    OSMBounds bounds;
    int event;
    boolean advanceAfter;
    Parseable parsing;

    public OSMReader(InputStream stream) throws XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        event = reader.next();

        parseBounds();

        parsing = Parseable.node;
        parseAll(Parseable.node, this::parseNode);
        System.out.println("Parsed: " + nodeRefs.size() + " nodes");

        parsing = Parseable.way;
        parseAll(Parseable.way, this::parseWay);
        System.out.println("Parsed: " + wayRefs.size() + " ways");

        parsing = Parseable.relation;
        parseAll(Parseable.relation, this::parseRelation);
        System.out.println("Parsed: " + relationWays.size() + " relations");
    }

    void advance() {
        try {
            event = reader.next();
        } catch (XMLStreamException e) {
            // A method that throws cannot be coerced to a Runnable.
            // Since we want to call advance from a Runnable, we must wrap exceptions.
            throw new RuntimeException(e);
        }
    }

    void parseBounds() {
        while (event != XMLStreamConstants.START_ELEMENT || !reader.getLocalName().equals("bounds")) {
            advance();
        }

        bounds =
                new OSMBounds(
                        getDouble("minlat"), getDouble("minlon"), getDouble("maxlat"), getDouble("maxlon"));
        advance();
    }

    void parseAll(Parseable parseable, Runnable runnable) {
        while (true) {
            advanceAfter = true;
            while (event != XMLStreamConstants.START_ELEMENT) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }

            if (!reader.getLocalName().equals(parseable.name())) {
                advanceAfter = false;
                return;
            }

            runnable.run();

            if (advanceAfter) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }
        }
    }

    void parseNode() {
        nodeRefs.put(getLong("id"), nodeCoords.size());
        nodeCoords.add((getDouble("lon") - (bounds.minlon() + bounds.maxlon()) / 2) * 5600);
        nodeCoords.add((getDouble("lat") - (bounds.minlat() + bounds.maxlat()) / 2) * 10000);
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.tag, this::parseTag);

        var tagEnd = tags.size();
        nodeTags.add(tagStart);
        nodeTags.add(tagEnd);
    }

    void parseWay() {
        var id = getLong("id");
        var way = wayNodes.size();
        wayRefs.put(id, way);
        wayNodes.add(new IntList());
        var wayDrawablesSize = wayDrawables.size();
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.nd, this::parseNd);
        parseAll(Parseable.tag, this::parseTag);

        var tagEnd = tags.size();
        wayTags.add(tagStart);
        wayTags.add(tagEnd);

        var diff = wayDrawables.size() - wayDrawablesSize;
        switch (diff) {
            case 0 -> wayDrawables.add(-1);
            case 1 -> {} // 1 is expected
            default -> { // Multiple drawing candidates... Choose first valid one
                var drawable = Drawable.Unknown;
                var i = tagStart;
                for (; i < tagEnd; i += 2) {
                    drawable = Drawable.fromTag(tags.get(i), tags.get(i + 1));
                    if (drawable != Drawable.Unknown) break;
                }

                if (drawable == Drawable.Unknown) i = -1;

                System.out.printf(
                        "Way with %s drawable tags (ignoring all except first): id=%d%n", diff, id);
                wayDrawables.set(way, i);
                wayDrawables.truncate(diff - 1);
            }
        }
    }

    void parseRelation() {
        relationWays.add(new IntList());
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.member, this::parseMember);
        parseAll(Parseable.tag, this::parseTag);

        var tagEnd = tags.size();
        relationTags.add(tagStart);
        relationTags.add(tagEnd);
    }

    void parseTag() {
        var k = get("k");
        var tag = Tag.from(k);
        if (tag == null) return;

        if (parsing == Parseable.way && tag.drawable) {
            wayDrawables.add(tags.size());
        }

        tags.add(k.intern());
        tags.add(get("v").intern());
    }

    void parseNd() {
        var last = wayNodes.get(wayNodes.size() - 1);
        last.add(nodeRefs.get(getLong("ref")));
    }

    void parseMember() {
        if (get("type").equals("way")) {
            var last = relationWays.get(relationWays.size() - 1);
            last.add(wayRefs.get(getLong("ref")));
        }
    }

    String get(String attr) {
        return reader.getAttributeValue(null, attr);
    }

    long getLong(String attr) {
        return Long.parseLong(get(attr));
    }

    double getDouble(String attr) {
        return Double.parseDouble(get(attr));
    }

    public Polygons createPolygons() {
        var polygons = new Polygons();

        var points = new ArrayList<Vector2D>();
        var values = wayRefs.values();
        for (int i = 0; i < values.size(); i++) {
            var way = values.get(i);
            var wd = wayDrawables.get(way);

            // Undrawable
            if (wd == -1) continue;

            var drawable = Drawable.fromTag(tags.get(wd), tags.get(wd + 1));
            if (drawable == Drawable.Ignored) continue;

            var nodes = wayNodes.get(way);
            for (int j = 0; j < nodes.size(); j++) {
                var node = nodes.get(j);
                if (node == -1) continue;
                points.add(new Vector2D(nodeCoords.get(node), nodeCoords.get(node + 1)));
            }

            switch (drawable.shape) {
                case Polyline -> polygons.addLines(points, drawable.size, drawable.color, drawable.layer());
                case Fill -> polygons.addPolygon(points, drawable.color, drawable.layer());
            }

            points.clear();
        }

        return polygons;
    }
}
