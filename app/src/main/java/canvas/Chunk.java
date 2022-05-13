package canvas;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import io.PartialChunk;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Chunk {
    private final int totalIndices;
    private final int totalVertices;
    private final int totalDrawables;
    public int count;
    private VBOWrapper indices;
    private VBOWrapper vertices;
    private VBOWrapper drawables;
    private int curIndex;
    private int curVertex;
    private int curDrawable;

    public Chunk(int totalIndices, int totalVertices, int totalDrawables) {
        this.totalIndices = totalIndices;
        this.totalVertices = totalVertices;
        this.totalDrawables = totalDrawables;

        count = totalIndices;
    }

    public void init(GLAutoDrawable glAutoDrawable) {
        indices =
                new VBOWrapper(glAutoDrawable, GL3.GL_ELEMENT_ARRAY_BUFFER, totalIndices * Integer.BYTES);
        vertices = new VBOWrapper(glAutoDrawable, GL3.GL_ARRAY_BUFFER, totalVertices * Float.BYTES);
        drawables = new VBOWrapper(glAutoDrawable, GL3.GL_ARRAY_BUFFER, totalDrawables * Byte.BYTES);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public VBOWrapper indices() {
        return indices;
    }

    public VBOWrapper vertices() {
        return vertices;
    }

    public VBOWrapper drawables() {
        return drawables;
    }

    public void add(PartialChunk part) {
        for (var drawing : part.drawings) {
            // Offset indices
            var iArray = drawing.indices().toArray();
            for (int i = 0; i < drawing.indices().size(); i++) {
                iArray[i] += curVertex / 2;
            }

            // Upload to VBOs
            indices().set(IntBuffer.wrap(iArray), curIndex, drawing.indices().size());
            vertices()
                    .set(
                            FloatBuffer.wrap(drawing.vertices().getArray()),
                            curVertex,
                            drawing.vertices().size());
            drawables()
                    .set(
                            ByteBuffer.wrap(drawing.drawables().getArray()),
                            curDrawable,
                            drawing.drawables().size());

            curIndex += drawing.indices().size();
            curVertex += drawing.vertices().size();
            curDrawable += drawing.drawables().size();
        }
    }

    public void dispose() {
        indices.dispose();
        vertices.dispose();
        drawables.dispose();
    }
}
