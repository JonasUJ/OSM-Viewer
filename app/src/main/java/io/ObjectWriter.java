package io;

import geometry.Rect;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Keeps an object in memory and writes it to the output stream at the end
 *
 * @param <T> Type of the object to write
 */
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
    public void onBounds(Rect bounds) {
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
