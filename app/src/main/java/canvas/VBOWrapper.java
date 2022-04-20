package canvas;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VBOWrapper {
    // private long capacity;
    private final int type;
    public final int vbo;
    private final GL3 gl;

    public VBOWrapper(GLAutoDrawable glAutoDrawable, int type) {
        this(glAutoDrawable, type, 0);
    }

    public VBOWrapper(GLAutoDrawable glAutoDrawable, int type, long initialCapacity) {
        this.type = type;
        // this.capacity = initialCapacity;
        GL3 gl = glAutoDrawable.getGL().getGL3();
        this.gl = gl;

        var buf = IntBuffer.allocate(1);
        gl.glGenBuffers(1, buf);
        vbo = buf.get(0);

        gl.glBindBuffer(type, vbo);
        gl.glBufferData(type, initialCapacity, null, GL3.GL_DYNAMIC_DRAW);
    }

    public void bind() {
        gl.glBindBuffer(type, vbo);
    }

    public void set(IntBuffer buffer, long offset, int length) {
        set(buffer, offset * Integer.BYTES, ((long) length) * Integer.BYTES);
    }

    public void set(FloatBuffer buffer, long offset, int length) {
        set(buffer, offset * Float.BYTES, ((long) length) * Float.BYTES);
    }

    public void set(ByteBuffer buffer, long offset, int length) {
        set(buffer, offset * Byte.BYTES, ((long) length) * Byte.BYTES);
    }

    private void set(Buffer buffer, long offset, long length) {
        // if (capacity < offset + length) grow(offset + length);

        bind();
        gl.glBufferSubData(type, offset, length, buffer);
    }

    public void dispose() {
        gl.glDeleteBuffers(1, new int[] {vbo}, 0);
    }

    // private void grow(long newSize) {
    //    while (capacity < newSize) capacity *= 2;

    //    GL3 gl = glAutoDrawable.getGL().getGL3();

    //    var buf = IntBuffer.allocate(1);
    //    gl.glGenBuffers(1, buf);
    //    var newVBO = buf.get(0);

    //    gl.glBindBuffer(GL3.GL_COPY_READ_BUFFER, vbo);
    //    gl.glGetBufferParameteriv(GL3.GL_COPY_READ_BUFFER, GL3.GL_BUFFER_SIZE, buf);
    //    gl.glBindBuffer(GL3.GL_COPY_WRITE_BUFFER, newVBO);
    //    gl.glBufferData(GL3.GL_COPY_WRITE_BUFFER, capacity, null, GL3.GL_DYNAMIC_DRAW);
    //    gl.glCopyBufferSubData(GL3.GL_COPY_READ_BUFFER, GL3.GL_COPY_WRITE_BUFFER, 0, 0, buf.get(0));
    //    gl.delete

    //    vbo = newVBO;
    // }
}
