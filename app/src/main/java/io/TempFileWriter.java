package io;

import java.io.*;

/**
 * A Writer that is backed by a temp file. When writeTo() is called the contents of the temp file is
 * transferred to the passed stream. Subclasses have access to the file stream and can write to it
 * continuously.
 */
public abstract class TempFileWriter implements Writer {
    protected final ObjectOutputStream stream;
    private final File file;

    public TempFileWriter() throws IOException {
        file = File.createTempFile("osm", "");
        file.deleteOnExit();
        stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
     * Called when we're done writing to the temp file. Closes the file stream and returns an input
     * stream of the file instead.
     *
     * @return Input stream of the underlying file
     * @throws IOException
     */
    private InputStream finish() throws IOException {
        stream.flush();
        stream.close();
        return new BufferedInputStream(new FileInputStream(file));
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        try (var in = finish()) {
            in.transferTo(out);
            out.flush();
        }
        file.delete();
    }
}
