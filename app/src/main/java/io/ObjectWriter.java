package io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import osm.OSMObserver;
import osm.elements.OSMBounds;
import osm.elements.OSMNode;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

public class ObjectWriter<T extends OSMObserver & Serializable> implements Writer {
    private final T obj;

    public ObjectWriter(T obj) {
        this.obj = obj;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        new ObjectOutputStream(out).writeObject(obj);
    }

    @Override
    public void onBounds(OSMBounds bounds) {
        obj.onBounds(bounds);
    }

    @Override
    public void onNode(OSMNode node) {
        obj.onNode(node);
    }

    @Override
    public void onWay(OSMWay way) {
        obj.onWay(way);
    }

    @Override
    public void onRelation(OSMRelation relation) {
        obj.onRelation(relation);
    }

    @Override
    public void onFinish() {
        obj.onFinish();
    }
}
