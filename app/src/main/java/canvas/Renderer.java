package canvas;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import java.io.File;
import java.nio.FloatBuffer;
import javafx.scene.paint.Color;
import osm.Drawable;
import shaders.Location;
import shaders.ShaderProgram;

public class Renderer implements GLEventListener {
    final Color clear = Drawable.Water.color;
    final Model model;
    final MapCanvas canvas;
    ShaderProgram shaderProgram;
    FloatBuffer orthographic;

    public Renderer(Model model, MapCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_BLEND);

        gl.glEnable(GL3.GL_MULTISAMPLE);

        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glEnable(GL3.GL_DEPTH_TEST);

        File vertexShader = new File("shaders/default.vs");
        File fragmentShader = new File("shaders/default.fs");

        shaderProgram = new ShaderProgram();
        shaderProgram.init(gl, vertexShader, fragmentShader);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(Model.VBOType.Vertex));
        gl.glVertexAttribPointer(
                shaderProgram.getLocation(Location.POSITION), 3, GL3.GL_FLOAT, false, Float.BYTES * 3, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.POSITION));

        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, model.getVBO(Model.VBOType.Index));

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(Model.VBOType.Color));
        gl.glVertexAttribPointer(
                shaderProgram.getLocation(Location.COLOR), 3, GL3.GL_FLOAT, false, Float.BYTES * 3, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.COLOR));
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        shaderProgram.dispose(gl);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        gl.glClearColor(
                (float) clear.getRed(), (float) clear.getGreen(), (float) clear.getBlue(), 1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram.getProgramId());

        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.TRANS), 1, false, canvas.getTransformBuffer().rewind());
        gl.glUniformMatrix4fv(
                shaderProgram.getLocation(Location.ORTHOGRAPHIC), 1, false, orthographic.rewind());

        gl.glDrawElements(GL3.GL_TRIANGLES, model.getCount(), GL3.GL_UNSIGNED_INT, 0);

        gl.glUseProgram(0);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
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
