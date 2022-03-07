package canvas;

import collections.Polygons;
import com.jogamp.opengl.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import osm.OSMReader;

public class Model {
    final GLCapabilities caps;
    final GLAutoDrawable sharedDrawable;
    final int[] vbo = new int[Model.VBOType.values().length];
    int count;

    public Model(String filename) throws IOException, XMLStreamException {
        Polygons polygons;

        // TODO: Clean up file reading/writing. Also, I was using Serializable before it
        // was cool...
        if (filename.endsWith(".osm")
                || filename.endsWith(".xml")
                || filename.endsWith(".osm.zip")
                || filename.endsWith(".xml.zip")) {

            InputStream stream = new BufferedInputStream(new FileInputStream(filename));
            if (filename.endsWith(".zip")) {
                var zipFile = new ZipFile(filename);
                var entry = zipFile.entries().nextElement();
                stream = zipFile.getInputStream(entry);
            }

            var reader = new OSMReader(stream);
            polygons = reader.createPolygons();

            filename = filename.split("\\.")[0] + ".ser.zip";
            var zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            zipStream.putNextEntry(new ZipEntry(filename));
            var outputStream = new ObjectOutputStream(zipStream);
            outputStream.writeObject(polygons);
            outputStream.flush();
            outputStream.close();
        } else if (filename.endsWith(".ser") || filename.endsWith(".ser.zip")) {
            try {
                InputStream stream;
                if (filename.endsWith(".zip")) {
                    var zipFile = new ZipFile(filename);
                    var entry = zipFile.entries().nextElement();
                    stream = zipFile.getInputStream(entry);
                } else {
                    stream = new BufferedInputStream(new FileInputStream(filename));
                }

                stream = new ObjectInputStream(stream);
                polygons = (Polygons) ((ObjectInputStream) stream).readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("Could not read serialized file: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException(
                    "Only .osm, .xml, .ser or any of those zipped are allowed");
        }

        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    var vertexBuffer = polygons.getVertexBuffer();
                    var indexBuffer = polygons.getIndexBuffer();
                    var colorBuffer = polygons.getColorBuffer();
                    count = indexBuffer.capacity();

                    gl.glGenBuffers(vbo.length, vbo, 0);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Vertex));
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER,
                            (long) vertexBuffer.capacity() * Float.BYTES,
                            vertexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(Model.VBOType.Index));
                    gl.glBufferData(
                            GL3.GL_ELEMENT_ARRAY_BUFFER,
                            (long) indexBuffer.capacity() * Float.BYTES,
                            indexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Color));
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER,
                            (long) colorBuffer.capacity() * Float.BYTES,
                            colorBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    return true;
                });

        sharedDrawable.display();
    }

    public GLCapabilities getCaps() {
        return caps;
    }

    public GLAutoDrawable getSharedDrawable() {
        return sharedDrawable;
    }

    public int getVBO(Model.VBOType type) {
        return vbo[type.ordinal()];
    }

    public int getCount() {
        return count;
    }

    enum VBOType {
        Vertex,
        Index,
        Color
    }
}
