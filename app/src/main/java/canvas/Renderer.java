package canvas;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import drawing.Drawable;
import java.io.File;
import java.nio.FloatBuffer;

import javafx.scene.paint.Color;
import shaders.Location;
import shaders.ShaderProgram;

public class Renderer implements GLEventListener {
    private final Color clear = Drawable.WATER.color;
    private final Model model;
    private final MapCanvas canvas;
    private ShaderProgram shaderProgram;
    private FloatBuffer orthographic;

    public Renderer(Model model, MapCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
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

        File vertexShader = new File("shaders/default.vert");
        File fragmentShader = new File("shaders/default.frag");

        shaderProgram = new ShaderProgram();
        shaderProgram.init(gl, vertexShader, fragmentShader);

        // Set the current index buffer to the index buffer from the model
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, model.getVBO(Model.VBOType.INDEX));

        // Set the current buffer to the vertex vbo
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(Model.VBOType.VERTEX));
        // Tell OpenGL that the current buffer holds position data. 3 floats per position.
        gl.glVertexAttribPointer(
                shaderProgram.getLocation(Location.POSITION), 3, GL3.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.POSITION));

        // Set the current buffer to the drawable vbo. We're done initialising the vertex vbo now.
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(Model.VBOType.DRAWABLE));
        // Tell OpenGL that the current buffer holds drawable data. 1 byte per drawable.
        gl.glVertexAttribIPointer(
                shaderProgram.getLocation(Location.DRAWABLE_ID), 1, GL3.GL_BYTE, 0, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.DRAWABLE_ID));
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        shaderProgram.dispose(gl);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        // Set the color used when clearing the screen
        gl.glClearColor(
                (float) clear.getRed(), (float) clear.getGreen(), (float) clear.getBlue(), 1.0f);
        // Clear the screen
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram.getProgramId());

        // Tell OpenGL about our transformation and orthographic matrices.
        // We need these in the vertex shader to position our vertices correctly.
        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.TRANSFORM), 1, false, canvas.getTransformBuffer().rewind());
        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.ORTHOGRAPHIC), 1, false, orthographic.rewind());

        gl.glActiveTexture(GL3.GL_TEXTURE0);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.COLOR_MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.COLOR_MAP), 0);

        gl.glActiveTexture(GL3.GL_TEXTURE1);
        gl.glBindTexture(GL3.GL_TEXTURE_1D, model.getTex(Model.TexType.MAP));
        gl.glUniform1i(shaderProgram.getLocation(Location.MAP), 1);

        gl.glUniform1ui(shaderProgram.getLocation(Location.CATEGORY_BITSET), canvas.categories.getFlags());

        // Draw `model.getCount()` many triangles
        // This will use the currently bound index buffer
        gl.glDrawElements(GL3.GL_TRIANGLES, model.getCount(), GL3.GL_UNSIGNED_INT, 0);

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
        this.orthographic =
                Buffers.newDirectFloatBuffer(
                        new float[] {
                            2 / (right - left),
                            0,
                            0,
                            0,
                            0,
                            -2 / (top - bottom),
                            0,
                            0,
                            0,
                            0,
                            -2 / (far - near),
                            0,
                            -(right + left) / (right - left),
                            -(top + bottom) / (top - bottom),
                            (far + near) / (far - near),
                            1
                        });
    }
}
