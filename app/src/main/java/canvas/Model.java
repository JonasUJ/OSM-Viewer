package canvas;

import collections.grid.Grid;
import com.jogamp.opengl.*;
import drawing.DrawableEnum;
import io.PolygonsReader;

import java.nio.IntBuffer;
import java.util.TreeMap;

public class Model {
    private final GLCapabilities caps;
    private final GLAutoDrawable sharedDrawable;
    private final IntBuffer tex = IntBuffer.allocate(TexType.values().length);
    private TreeMap<Float, Grid<Chunk>> chunks;
    private Chunk baseChunk;

    public Model(PolygonsReader reader) {
        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        // sharedDrawable is the object we communicate with OpenGL through
        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        loadPolygons(reader);
    }

    private void loadPolygons(PolygonsReader reader) {
        // Run once. We upload our various buffers to the GPU, registering them with OpenGL
        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    // Get new id's for textures
                    gl.glGenTextures(tex.capacity(), tex);

                    // Upload COLOR_MAP as 1D RGBA texture
                    gl.glActiveTexture(GL3.GL_TEXTURE0);
                    gl.glBindTexture(GL3.GL_TEXTURE_1D, getTex(TexType.COLOR_MAP));
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
                    gl.glTexImage1D(
                            GL3.GL_TEXTURE_1D,
                            0,
                            GL3.GL_RGBA,
                            DrawableEnum.values().length,
                            0,
                            GL3.GL_RGBA,
                            GL3.GL_FLOAT,
                            DrawableEnum.COLOR_MAP.rewind());

                    gl.glActiveTexture(GL3.GL_TEXTURE1);
                    gl.glBindTexture(GL3.GL_TEXTURE_1D, getTex(TexType.MAP));
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
                    gl.glTexImage1D(
                            GL3.GL_TEXTURE_1D,
                            0,
                            GL3.GL_RG32UI,
                            DrawableEnum.values().length,
                            0,
                            GL3.GL_RG_INTEGER,
                            GL3.GL_UNSIGNED_INT,
                            DrawableEnum.MAP.rewind());

                    // Prepare chunks map for data
                    chunks = new TreeMap<>();
                    var map = reader.getChunks();
                    for (var m : map.entrySet()) {
                        for (var chunk : m.getValue().values()) {
                            chunk.init(glAutoDrawable);
                        }

                        chunks.put(m.getKey(), new Grid<>(reader.getBounds(), m.getKey(), m.getValue()));
                    }

                    // Read all data
                    for (var partialChunk : reader.read()) {
                        var chunk = map.get(partialChunk.cellSize).get(partialChunk.point);

                        // Upload partial chunk
                        chunk.add(partialChunk);
                    }

                    var partialBaseChunk = reader.getBaseChunk();
                    baseChunk =
                            new Chunk(
                                    partialBaseChunk.getTotalIndices(),
                                    partialBaseChunk.getTotalVertices(),
                                    partialBaseChunk.getTotalDrawables());
                    baseChunk.init(glAutoDrawable);
                    baseChunk.add(partialBaseChunk);

                    System.gc();

                    return true;
                });
    }

    public GLCapabilities getCaps() {
        return caps;
    }

    public GLAutoDrawable getSharedDrawable() {
        return sharedDrawable;
    }

    public int getTex(Model.TexType type) {
        return tex.get(type.ordinal());
    }

    public void dispose() {
        for (int i = 0; i < sharedDrawable.getGLEventListenerCount(); i++) {
            sharedDrawable.disposeGLEventListener(sharedDrawable.getGLEventListener(i), false);
        }

        sharedDrawable.invoke(
                false,
                glAutoDrawable -> {
                    for (var grid : chunks.values()) {
                        for (var chunk : grid) {
                            chunk.dispose();
                        }
                    }

                    return true;
                });
    }

    public TreeMap<Float, Grid<Chunk>> getAllChunks() {
        return chunks;
    }

    public Grid<Chunk> getChunks(float size) {
        var entry = chunks.floorEntry(size);

        if (entry == null) {
            entry = chunks.firstEntry();
        }

        return entry.getValue();
    }

    public Chunk getBaseChunk() {
        return baseChunk;
    }

    enum TexType {
        COLOR_MAP,
        MAP,
    }
}
