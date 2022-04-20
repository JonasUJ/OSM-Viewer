package canvas;

import collections.Entity;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import drawing.Drawable;
import drawing.Drawing;
import drawing.DrawingManager;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import shaders.Location;
import shaders.ShaderProgram;

public class Renderer implements GLEventListener {
    public enum Shader {
        DEFAULT("shaders/default.frag"),
        MONOCHROME("shaders/monochrome.frag"),
        PARTY("shaders/party.frag");

        public final String filename;

        Shader(String filename) {
            this.filename = filename;
        }
    }

    private final DrawingManager manager = new DrawingManager();
    private final Color clear = Drawable.WATER.color;
    private final Model model;
    private final MapCanvas canvas;
    private final ShaderProgram[] shaderPrograms = new ShaderProgram[Shader.values().length];
    private Shader shader;
    private Shader nextShader;
    private Affine projection;
    private VBOWrapper indexVBO;
    private VBOWrapper vertexVBO;
    private VBOWrapper drawableVBO;

    public Renderer(Model model, MapCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
    }

    /**
     * Add a drawing to the renderer, causing it to be rendered from the next frame onwards.
     *
     * @param drawing The drawing to render.
     * @return The actual drawing that was rendered. Can be used with `clear` to later remove this
     *     drawing from the renderer.
     */
    public Drawing draw(Drawing drawing) {
        var info = manager.draw(drawing);
        updateInfo(info);
        return info.drawing();
    }

    /** Clear all drawings from the renderer. */
    public void clear() {
        manager.clear();
    }

    /**
     * Clear a drawing from the renderer.
     *
     * @param drawing The drawing to stop rendering.
     */
    public void clear(Entity drawing) {
        var info = manager.clear(drawing);
        updateInfo(info);
    }

    private void updateInfo(DrawingManager.DrawingInfo info) {
        // Upload new triangles to OpenGL
        model
                .getSharedDrawable()
                .invoke(
                        true,
                        glAutoDrawable -> {
                            indexVBO.set(
                                    IntBuffer.wrap(info.drawing().indices().getArray()),
                                    info.indicesStart(),
                                    info.drawing().indices().size());
                            vertexVBO.set(
                                    FloatBuffer.wrap(info.drawing().vertices().getArray()),
                                    info.verticesStart(),
                                    info.drawing().vertices().size());
                            drawableVBO.set(
                                    ByteBuffer.wrap(info.drawing().drawables().getArray()),
                                    info.drawablesStart(),
                                    info.drawing().drawables().size());
                            return true;
                        });
    }

    public void setShader(Shader newShader) {
        nextShader = newShader;
    }

    private ShaderProgram setShader(
            GLAutoDrawable glAutoDrawable, VBOWrapper vertexVBO, VBOWrapper drawableVBO) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

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
                new float[] {
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
        indexVBO =
                new VBOWrapper(glAutoDrawable, GL3.GL_ELEMENT_ARRAY_BUFFER, sizeEstimate * Integer.BYTES);
        vertexVBO = new VBOWrapper(glAutoDrawable, GL3.GL_ARRAY_BUFFER, sizeEstimate * Float.BYTES);
        drawableVBO =
                new VBOWrapper(glAutoDrawable, GL3.GL_ARRAY_BUFFER, sizeEstimate / 2 * Byte.BYTES);

        // Set the current index buffer to the index buffer from the model
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, model.getVBO(Model.VBOType.INDEX).vbo);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        for (var shaderProgram : shaderPrograms) {
            shaderProgram.dispose(gl);
        }
        indexVBO.dispose();
        vertexVBO.dispose();
        drawableVBO.dispose();
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        model.getVBO(Model.VBOType.INDEX).bind();
        ShaderProgram shaderProgram =
                setShader(
                        glAutoDrawable,
                        model.getVBO(Model.VBOType.VERTEX),
                        model.getVBO(Model.VBOType.DRAWABLE));

        // Set the color used when clearing the screen
        gl.glClearColor(
                (float) clear.getRed(), (float) clear.getGreen(), (float) clear.getBlue(), 1.0f);
        // Clear the screen
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram.getProgramId());

        // Tell OpenGL about our projection matrix.
        // We need this in the vertex shader to position our vertices correctly.
        var transform = canvas.transform.clone();
        transform.prepend(projection);
        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.PROJECTION), 1, false, affineToBuffer(transform));

        gl.glActiveTexture(GL3.GL_TEXTURE0);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.COLOR_MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.COLOR_MAP), 0);

        gl.glActiveTexture(GL3.GL_TEXTURE1);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.MAP), 1);

        gl.glUniform1ui(
                shaderProgram.getLocation(Location.CATEGORY_BITSET), canvas.categories.getFlags());
        gl.glUniform1f(
                shaderProgram.getLocation(Location.TIME),
                (float) (System.currentTimeMillis() % (2000 * Math.PI) / 1000.0));

        // Draw `model.getCount()` many triangles
        // This will use the currently bound index buffer
        gl.glDrawElements(GL3.GL_TRIANGLES, model.getCount(), GL3.GL_UNSIGNED_INT, 0);

        indexVBO.bind();
        shaderProgram = setShader(glAutoDrawable, vertexVBO, drawableVBO);

        gl.glUseProgram(shaderProgram.getProgramId());

        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.PROJECTION), 1, false, affineToBuffer(transform));

        gl.glActiveTexture(GL3.GL_TEXTURE0);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.COLOR_MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.COLOR_MAP), 0);

        gl.glActiveTexture(GL3.GL_TEXTURE1);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.MAP), 1);

        gl.glUniform1ui(
                shaderProgram.getLocation(Location.CATEGORY_BITSET), canvas.categories.getFlags());
        gl.glUniform1f(
                shaderProgram.getLocation(Location.TIME),
                (float) (System.currentTimeMillis() % (2000 * Math.PI) / 1000.0));

        // Draw `manager.drawing().indices().size()` many triangles
        // This will use the currently bound index buffer
        gl.glDrawElements(GL3.GL_TRIANGLES, manager.drawing().indices().size(), GL3.GL_UNSIGNED_INT, 0);

        gl.glUseProgram(0);
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
                        new double[] {
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
}
