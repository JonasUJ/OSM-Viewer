package canvas;

import collections.Entity;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import drawing.Drawable;
import drawing.DrawableDetailWrapper;
import drawing.DrawableEnum;
import drawing.DrawingManager;
import geometry.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.util.Pair;
import shaders.Location;
import shaders.ShaderProgram;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.TreeMap;

public class Renderer implements GLEventListener {
    private final Color clear = DrawableEnum.WATER.color();
    private final Model model;
    private final MapCanvas canvas;
    private final ShaderProgram[] shaderPrograms = new ShaderProgram[Shader.values().length];
    private Shader shader;
    private Shader nextShader;
    private Affine projection;
    private TreeMap<Float, Pair<DrawingManager, Chunk>> drawingChunks;

    public Renderer(Model model, MapCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
    }

    /**
     * Add a drawing to the renderer, causing it to be rendered from the next frame onwards.
     *
     * @param points   The points to draw.
     * @param drawable The Drawable describing the points.
     * @return An entity that can be used with `clear` to later remove this drawing from the renderer.
     */
    public Entity draw(List<Vector2D> points, Drawable drawable) {
        return draw((Object) points, drawable);
    }

    public Entity draw(Vector2D point, Drawable drawable) {
        return draw((Object) point, drawable);
    }

    @SuppressWarnings("unchecked")
    private Entity draw(Object points, Drawable drawable) {
        long id = 0;
        var detail = 0;
        for (var pair : drawingChunks.values()) {
            var manager = pair.getKey();
            var chunk = pair.getValue();

            DrawingManager.DrawingInfo info;

            // ugly ugly yuck but I just need this to work now
            if (points instanceof Vector2D point) {
                info = manager.draw(point, DrawableDetailWrapper.from(drawable, detail));
            } else if (points instanceof List list) {
                info = manager.draw((List<Vector2D>) list, DrawableDetailWrapper.from(drawable, detail));
            } else {
                return null; // Doesnt happen
            }

            if (id == 0) {
                id = info.drawing().id();
            } else {
                info.drawing().setId(id);
            }

            chunk.setCount(manager.drawing().indices().size());
            updateInfo(info, chunk);
            detail++;
        }

        return Entity.withId(id);
    }

    /**
     * Clear all drawings from the renderer.
     */
    public void clear() {
        for (var pair : drawingChunks.values()) {
            pair.getKey().clear();
            pair.getValue().setCount(0);
        }
    }

    /**
     * Clear a drawing from the renderer.
     *
     * @param drawing The drawing to stop rendering.
     */
    public void clear(Entity drawing) {
        for (var pair : drawingChunks.values()) {
            var manager = pair.getKey();
            var chunk = pair.getValue();

            var info = manager.clear(drawing);
            chunk.setCount(manager.drawing().indices().size());
            updateInfo(info, chunk);
        }
    }

    private void updateInfo(DrawingManager.DrawingInfo info, Chunk chunk) {
        // Upload new triangles to OpenGL
        model
                .getSharedDrawable()
                .invoke(
                        true,
                        glAutoDrawable -> {
                            chunk
                                    .indices()
                                    .set(
                                            IntBuffer.wrap(info.drawing().indices().getArray()),
                                            info.indicesStart(),
                                            info.drawing().indices().size());
                            chunk
                                    .vertices()
                                    .set(
                                            FloatBuffer.wrap(info.drawing().vertices().getArray()),
                                            info.verticesStart(),
                                            info.drawing().vertices().size());
                            chunk
                                    .drawables()
                                    .set(
                                            ByteBuffer.wrap(info.drawing().drawables().getArray()),
                                            info.drawablesStart(),
                                            info.drawing().drawables().size());
                            return true;
                        });
    }

    public void setShader(Shader newShader) {
        nextShader = newShader;
    }

    private ShaderProgram setShader(GL3 gl, VBOWrapper vertexVBO, VBOWrapper drawableVBO) {

        shader = nextShader;
        var shaderProgram = shaderPrograms[shader.ordinal()];

        // Set the current buffer to the vertex vbo
        vertexVBO.bind();
        // Tell OpenGL that the current buffer holds position data. 2 floats per position.
        gl.glVertexAttribPointer(
                shaderProgram.getLocation(Location.POSITION), 2, GL3.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.POSITION));

        // Set the current buffer to the drawable vbo. We're done initialising the vertex vbo now.
        drawableVBO.bind();
        // Tell OpenGL that the current buffer holds drawable data. 1 byte per drawable.
        gl.glVertexAttribIPointer(
                shaderProgram.getLocation(Location.DRAWABLE_ID), 1, GL3.GL_BYTE, 0, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.DRAWABLE_ID));

        return shaderProgram;
    }

    private FloatBuffer affineToBuffer(Affine affine) {
        return FloatBuffer.wrap(
                new float[]{
                        (float) affine.getMxx(),
                        (float) affine.getMyx(),
                        (float) affine.getMzx(),
                        0,
                        (float) affine.getMxy(),
                        (float) affine.getMyy(),
                        (float) affine.getMzy(),
                        0,
                        (float) affine.getMzx(),
                        (float) affine.getMzy(),
                        (float) affine.getMzz(),
                        0,
                        (float) affine.getTx(),
                        (float) affine.getTy(),
                        (float) affine.getTz(),
                        1
                });
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        // Enable transparency. Makes things visible through other things if they are not completely
        // opaque.
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_BLEND);

        // Enable anti aliasing. Smooths jagged lines on low resolution screens.
        gl.glEnable(GL3.GL_MULTISAMPLE);

        // Enable depth. We need this to know which things are drawn atop others.
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glEnable(GL3.GL_DEPTH_TEST);

        // Enable textures. We need this for the mappings.
        gl.glEnable(GL3.GL_TEXTURE_1D);

        // Load shaders
        int i = 0;
        File vertexShader = new File("shaders/default.vert");
        for (var shader : Shader.values()) {
            File fragmentShader = new File(shader.filename);
            var shaderProgram = new ShaderProgram();
            shaderProgram.init(gl, vertexShader, fragmentShader);
            shaderPrograms[i++] = shaderProgram;
        }

        setShader(Shader.DEFAULT);

        var sizeEstimate = 2 * 1024 * 1024;

        drawingChunks = new TreeMap<>();
        for (var size : model.getAllChunks().keySet()) {
            var manager = new DrawingManager();
            var chunk = new Chunk(sizeEstimate, sizeEstimate, sizeEstimate / 2);
            chunk.init(glAutoDrawable);
            chunk.setCount(0);
            drawingChunks.put(size, new Pair<>(manager, chunk));
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        for (var shaderProgram : shaderPrograms) {
            shaderProgram.dispose(gl);
        }

        for (var pair : drawingChunks.values()) {
            pair.getValue().dispose();
        }
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        // Set the color used when clearing the screen
        gl.glClearColor(
                (float) clear.getRed(), (float) clear.getGreen(), (float) clear.getBlue(), 1.0f);
        // Clear the screen
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        var transform = canvas.transform.clone();
        transform.prepend(projection);
        var transformBuffer = affineToBuffer(transform);
        var time = (float) (System.currentTimeMillis() % (2000 * Math.PI) / 1000.0);

        // Render base chunk
        draw(gl, model.getBaseChunk(), transformBuffer, time);

        // Render each chunk on screen
        for (var chunk : canvas.getChunks()) {
            if (chunk == null) continue;

            draw(gl, chunk, transformBuffer, time);
        }

        // Draw additional triangles added at runtime
        var entry = drawingChunks.floorEntry(canvas.chunkSize());

        if (entry == null) {
            entry = drawingChunks.firstEntry();
        }

        var drawingChunk = entry.getValue().getValue();
        draw(gl, drawingChunk, transformBuffer, time);

        gl.glUseProgram(0);
    }

    // TODO: If we had some more time I'd set this up to use VAOs, then this method would only be two
    // or three lines <3. I didn't know about those when we started the project though.
    private void draw(GL3 gl, Chunk chunk, FloatBuffer transform, float time) {
        chunk.indices().bind();

        var shaderProgram = setShader(gl, chunk.vertices(), chunk.drawables());
        gl.glUseProgram(shaderProgram.getProgramId());

        // Tell OpenGL about our projection matrix.
        // We need this in the vertex shader to position our vertices correctly.
        gl.glUniformMatrix4fv(shaderProgram.getLocation(Location.PROJECTION), 1, false, transform);

        gl.glActiveTexture(GL3.GL_TEXTURE0);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.COLOR_MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.COLOR_MAP), 0);

        gl.glActiveTexture(GL3.GL_TEXTURE1);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.MAP), 1);

        gl.glUniform1ui(
                shaderProgram.getLocation(Location.CATEGORY_BITSET), canvas.categories.getFlags());
        gl.glUniform1f(shaderProgram.getLocation(Location.TIME), time);

        // Draw triangles
        // This will use the currently bound index buffer
        gl.glDrawElements(GL3.GL_TRIANGLES, chunk.count, GL3.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        // When the window changes size, we need to recalculate the orthographic projection matrix
        // See this for some idea of how that works:
        // https://learnwebgl.brown37.net/08_projections/projections_ortho.html

        final float left = 0.0f;
        final float right = width;
        final float top = 0.0f;
        final float bottom = height;
        final float near = 0.0f;
        final float far = 1.0f;

        // Recalculate orthographic projection matrix
        projection =
                new Affine(
                        new double[]{
                                2 / (right - left),
                                0,
                                0,
                                -(right + left) / (right - left),
                                0,
                                2 / (top - bottom),
                                0,
                                -(top + bottom) / (top - bottom),
                                0,
                                0,
                                -2 / (far - near),
                                (far + near) / (far - near),
                        },
                        MatrixType.MT_3D_3x4,
                        0);
    }

    public enum Shader {
        DEFAULT("shaders/default.frag"),
        MONOCHROME("shaders/monochrome.frag"),
        PARTY("shaders/party.frag");

        public final String filename;

        Shader(String filename) {
            this.filename = filename;
        }
    }
}
