// Modified version of the below
// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

import com.jogamp.opengl.GL3;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Shader program utilities.
 *
 * @author serhiy
 */
public class ShaderUtils {

    private ShaderUtils() {
        /* Prevent initialization, only static methods below. */
    }

    /**
     * Loads the resource.
     *
     * @param fileName of the resource to load.
     * @return content of the resource converted to UTF-8 text.
     * @throws Exception when an error occurs loading resource.
     */
    public static String loadResource(String fileName) throws Exception {
        try (InputStream in = ShaderUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            return new Scanner(in, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        }
    }

    /**
     * Creates and compile the shader in the shader program.
     *
     * @param gl context.
     * @param programId to create its shaders.
     * @param shaderCode to compile.
     * @param shaderType of the shader to be compiled.
     * @return the id of the created and compiled shader.
     * @throws Exception when an error occurs creating the shader program.
     */
    public static int createShader(GL3 gl, int programId, String shaderCode, int shaderType)
            throws Exception {
        int shaderId = gl.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Shader id is zero.");
        }

        gl.glShaderSource(shaderId, 1, new String[] {shaderCode}, null);
        gl.glCompileShader(shaderId);

        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetShaderiv(shaderId, GL3.GL_COMPILE_STATUS, intBuffer);

        if (intBuffer.get(0) != 1) {
            gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            if (size > 0) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetShaderInfoLog(shaderId, size, intBuffer, byteBuffer);
                System.out.println(new String(byteBuffer.array()));
            }
            throw new Exception("Error compiling shader!");
        }

        gl.glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Links the shaders within created shader program.
     *
     * @param gl context.
     * @param programId to link its shaders.
     * @throws Exception when an error occurs linking the shaders.
     */
    public static void link(GL3 gl, int programId) throws Exception {
        gl.glLinkProgram(programId);

        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(programId, GL3.GL_LINK_STATUS, intBuffer);
        if (intBuffer.get(0) != 1) {
            gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            if (size > 0) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(programId, size, intBuffer, byteBuffer);
                System.out.println(new String(byteBuffer.array()));
            }
            throw new Exception("Error linking shader program!");
        }

       
    }
}
