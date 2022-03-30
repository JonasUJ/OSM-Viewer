package osm;

import geometry.Rect;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import osm.elements.*;
import osm.tables.NodeTable;
import osm.tables.RelationTable;
import osm.tables.WayTable;

public class OSMReader {
    enum Parseable {
        node,
        way,
        relation,
        tag,
        nd,
        member
    }

    private final List<OSMObserver> observers = new ArrayList<>();
    private XMLStreamReader reader;
    private int event;

    private final NodeTable nodes = new NodeTable();
    private final WayTable ways = new WayTable();
    private final RelationTable relations = new RelationTable();

    private OSMElement current;
    private final List<SlimOSMNode> wayNdList = new ArrayList<>();
    // IntelliJ's static analysis says this can be made local. Don't be fooled - it can't.
    private boolean advanceAfter;

    public OSMReader() {
        addObservers(nodes, ways, relations, new ReaderStats());
    }

    public void parse(InputStream stream) throws XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        event = reader.next();

        parseBounds();

        parseAll(Parseable.node, this::parseNode);
        parseAll(Parseable.way, this::parseWay);
        parseAll(Parseable.relation, this::parseRelation);

        for (var observer : observers) {
            observer.onFinish();
        }
    }

    public void addObservers(OSMObserver... observers) {
        this.observers.addAll(Arrays.asList(observers));
    }

    private void advance() {
        try {
            event = reader.next();
        } catch (XMLStreamException e) {
            // A method that throws cannot be coerced to a Runnable.
            // Since we want to call advance from a Runnable, we must wrap exceptions.
            // TODO: Wrap in custom exception that can be caught and shown to the user, informing
            // them of
            // malformed data
            throw new RuntimeException(e);
        }
    }

    private void parseBounds() {
        while (event != XMLStreamConstants.START_ELEMENT || !reader.getLocalName().equals("bounds")) {
            advance();
        }

        var bounds =
                new Rect(
                        getDouble("minlat"), getDouble("minlon"), getDouble("maxlat"), getDouble("maxlon"));
        advance();

        for (var observer : observers) {
            observer.onBounds(bounds);
        }
    }

    private void parseAll(Parseable parseable, Runnable runnable) {
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

    private void parseNode() {
        var node = new OSMNode(getLong("id"), getDouble("lon"), getDouble("lat"));
        current = node;

        advance();
        parseAll(Parseable.tag, this::parseTag);

        for (var observer : observers) {
            observer.onNode(node);
        }
    }

    private void parseWay() {
        var way = new OSMWay(getLong("id"));
        current = way;

        advance();
        parseAll(Parseable.nd, this::parseNd);
        parseAll(Parseable.tag, this::parseTag);

        way.setNodes(wayNdList.toArray(new SlimOSMNode[0]));
        wayNdList.clear();

        for (var observer : observers) {
            observer.onWay(way);
        }
    }

    private void parseRelation() {
        var relation = new OSMRelation(getLong("id"));
        current = relation;

        advance();
        parseAll(Parseable.member, this::parseMember);
        parseAll(Parseable.tag, this::parseTag);

        for (var observer : observers) {
            observer.onRelation(relation);
        }
    }

    private void parseTag() {
        var tag = OSMTag.from(get("k"), get("v").intern());
        if (tag == null) return;
        current.tags().add(tag);
    }

    private void parseNd() {
        var node = nodes.get(getLong("ref"));
        if (node == null) return;
        wayNdList.add(node);
    }

    private void parseMember() {
        if (get("type").equals("way")) {
            var way = ways.get(getLong("ref"));
            var member = OSMMemberWay.from(way, get("role"));
            if (member == null) return;
            ((OSMRelation) current).members().add(member);
        }
    }

    private String get(String attr) {
        return reader.getAttributeValue(null, attr);
    }

    private long getLong(String attr) {
        return Long.parseLong(get(attr));
    }

    private double getDouble(String attr) {
        return Double.parseDouble(get(attr));
    }
}
