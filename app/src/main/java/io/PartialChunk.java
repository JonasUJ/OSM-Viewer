package io;

import drawing.Drawing;
import geometry.Point;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PartialChunk implements Serializable {
    public final List<Drawing> drawings;

    public final Point point;
    public final float cellSize;
    private int byteSize = 0;
    private int totalIndices = 0;
    private int totalVertices = 0;
    private int totalDrawables = 0;

    public PartialChunk(Point point, float cellSize) {
        this.point = point;
        this.cellSize = cellSize;
        drawings = new ArrayList<>();
    }

    public void add(Drawing drawing) {
        drawings.add(drawing);
        byteSize += drawing.byteSize();

        totalIndices += drawing.indices().size();
        totalVertices += drawing.vertices().size();
        totalDrawables += drawing.drawables().size();
    }

    public int byteSize() {
        return byteSize;
    }

    public void flush(ObjectOutputStream out) {
        try {
            out.writeUnshared(this);
            out.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        drawings.clear();
        byteSize = 0;
    }

    public int getTotalIndices() {
        return totalIndices;
    }

    public int getTotalVertices() {
        return totalVertices;
    }

    public int getTotalDrawables() {
        return totalDrawables;
    }
}
