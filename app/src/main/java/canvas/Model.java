package canvas;

import com.jogamp.opengl.*;
import drawing.Drawable;
import io.FileParser;
import io.PolygonsReader;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Model {
    private final GLCapabilities caps;
    private final GLAutoDrawable sharedDrawable;
    private final IntBuffer vbo = IntBuffer.allocate(Model.VBOType.values().length);
    private final IntBuffer tex = IntBuffer.allocate(1);
    private int indexCount;

    public Model(String filename) throws Exception {
        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        // sharedDrawable is the object we communicate with OpenGL through
        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        try (var result = FileParser.readFile(filename)) {
            loadPolygons(result.polygons());
        }
    }

    private void loadPolygons(PolygonsReader reader) {
        // Run once. We upload our various buffers to the GPU, registering them with OpenGL
        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    indexCount = reader.getIndexCount();
                    var vertexCount = reader.getVertexCount();
                    var drawableCount = reader.getDrawableCount();

                    // Get new id's for the buffers
                    gl.glGenBuffers(vbo.capacity(), vbo);

                    // Pre-allocate buffers with correct size
                    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(VBOType.INDEX));
                    gl.glBufferData(
                            GL3.GL_ELEMENT_ARRAY_BUFFER,
                            (long) indexCount * Integer.BYTES,
                            null,
                            GL.GL_DYNAMIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.VERTEX));
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER, (long) vertexCount * Float.BYTES, null, GL.GL_DYNAMIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.DRAWABLE));
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER, (long) drawableCount * Byte.BYTES, null, GL.GL_DYNAMIC_DRAW);

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
                            Drawable.values().length,
                            0,
                            GL3.GL_RGBA,
                            GL3.GL_FLOAT,
                            Drawable.COLOR_MAP.rewind());

                    var curIndex = 0;
                    var curVertex = 0;
                    var curDrawable = 0;

                    for (var drawing : reader.read()) {
                        // Upload chunk to the index buffer
                        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(VBOType.INDEX));
                        gl.glBufferSubData(
                                GL3.GL_ELEMENT_ARRAY_BUFFER,
                                (long) curIndex * Integer.BYTES,
                                (long) drawing.indices().size() * Integer.BYTES,
                                IntBuffer.wrap(drawing.indices().getArray()));

                        // Upload chunk to the vertex buffer
                        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.VERTEX));
                        gl.glBufferSubData(
                                GL3.GL_ARRAY_BUFFER,
                                (long) curVertex * Float.BYTES,
                                (long) drawing.vertices().size() * Float.BYTES,
                                FloatBuffer.wrap(drawing.vertices().getArray()));

                        // Upload chunk to the drawable buffer
                        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.DRAWABLE));
                        gl.glBufferSubData(
                                GL3.GL_ARRAY_BUFFER,
                                (long) curDrawable * Byte.BYTES,
                                (long) drawing.drawables().size() * Byte.BYTES,
                                ByteBuffer.wrap(drawing.drawables().getArray()));

                        curIndex += drawing.indices().size();
                        curVertex += drawing.vertices().size();
                        curDrawable += drawing.drawables().size();
                    }

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

    /**
     * Get the generated id of the buffer with the given type
     *
     * @param type
     * @return Buffer id as seen from OpenGL
     */
    public int getVBO(Model.VBOType type) {
        return vbo.get(type.ordinal());
    }

    public int getTex(Model.TexType type) {
        return tex.get(type.ordinal());
    }

    /**
     * Get the amount of vertices
     *
     * @return How many vertices are stored in the model
     */
    public int getCount() {
        return indexCount;
    }

    enum VBOType {
        VERTEX, INDEX, DRAWABLE,
    }

    enum TexType {
        COLOR_MAP,
    }
}
