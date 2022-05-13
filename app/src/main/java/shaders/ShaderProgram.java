// Modified version of the below
// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

import com.jogamp.opengl.GL3;

import java.io.File;

/**
 * Manages the shader program.
 *
 * @author serhiy
 */
public class ShaderProgram {
    private final int[] locations = new int[Location.values().length];
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private boolean initialized = false;

    /**
     * Initializes the shader program.
     *
     * @param gl             context.
     * @param vertexShader   file.
     * @param fragmentShader file.
     * @return true if initialization was successful, false otherwise.
     */
    public void init(GL3 gl, File vertexShader, File fragmentShader) {
        if (initialized) {
            throw new IllegalStateException(
                    "Unable to initialize the shader program! (it was already initialized)");
        }

        try {
            String vertexShaderCode = ShaderUtils.loadResource(vertexShader.getName());
            String fragmentShaderCode = ShaderUtils.loadResource(fragmentShader.getName());

            programId = gl.glCreateProgram();
            vertexShaderId =
                    ShaderUtils.createShader(gl, programId, vertexShaderCode, GL3.GL_VERTEX_SHADER);
            fragmentShaderId =
                    ShaderUtils.createShader(gl, programId, fragmentShaderCode, GL3.GL_FRAGMENT_SHADER);

            ShaderUtils.link(gl, programId);

            for (var loc : Location.values()) {
                switch (loc.getType()) {
                    case ATTRIB -> locations[loc.ordinal()] =
                            gl.glGetAttribLocation(programId, loc.getName());
                    case UNIFORM -> locations[loc.ordinal()] =
                            gl.glGetUniformLocation(programId, loc.getName());
                }
            }

            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!initialized) {
            throw new IllegalStateException("Unable to initiate the shaders!");
        }
    }

    /**
     * Destroys the shader program.
     *
     * @param gl context.
     */
    public void dispose(GL3 gl) {
        initialized = false;
        gl.glDetachShader(programId, vertexShaderId);
        gl.glDetachShader(programId, fragmentShaderId);
        gl.glDeleteProgram(programId);
    }

    /**
     * @return shader program id.
     */
    public int getProgramId() {
        if (!initialized) {
            throw new IllegalStateException(
                    "Unable to get the program id! The shader program was not initialized!");
        }
        return programId;
    }

    /**
     * @param location to retrieve its location.
     * @return location of the shader attribute.
     */
    public int getLocation(Location location) {
        if (!initialized) {
            throw new IllegalStateException(
                    "Unable to get the attribute location! The shader program was not initialized!");
        }
        return locations[location.ordinal()];
    }

    public boolean isInitialized() {
        return initialized;
    }
}
