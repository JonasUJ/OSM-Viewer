package io;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javafx.util.Pair;
import javax.xml.stream.XMLStreamException;
import navigation.Dijkstra;
import osm.OSMReader;

// TODO: Use a custom exception type instead of RuntimeException for when parsing fails.
public class FileParser implements IOConstants {
    private static final String EXT = ".zip";
    private static final String POLYGONS = "polygons";
    private static final String DIJKSTRA = "dijkstra";

    public static ReadResult readFile(String filename) throws IOException, XMLStreamException {
        if (filename.matches(".*(\\.osm|\\.xml)(\\.zip)?$")) {
            filename = createMapFromOsm(filename);
            System.gc();
        }

        if (filename.endsWith(EXT)) {
            return readMap(filename);
        }

        throw new IllegalArgumentException(
                "Only .osm, .xml, any of those zipped, or " + EXT + " are allowed");
    }

    private static String createMapFromOsm(String infile) throws IOException, XMLStreamException {
        var reader = new OSMReader();
        var polygonsWriter = new PolygonsWriter();
        var dijkstraWriter = new ObjectWriter<>(new Dijkstra());
        reader.addObservers(polygonsWriter, dijkstraWriter);

        reader.parse(getInputStream(infile));

        var outfile = infile.split("\\.")[0] + EXT;
        createMapFromWriters(
                outfile, new Pair<>(POLYGONS, polygonsWriter), new Pair<>(DIJKSTRA, dijkstraWriter));

        return outfile;
    }

    @SafeVarargs
    private static void createMapFromWriters(String outfile, Pair<String, Writer>... pairs)
            throws IOException {
        try (var zipStream =
                new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outfile), BUFFER_SIZE))) {
            for (var pair : pairs) {
                zipStream.putNextEntry(new ZipEntry(pair.getKey()));
                pair.getValue().writeTo(zipStream);
                zipStream.flush();
            }
        }
    }

    private static ReadResult readMap(String filename) throws IOException {
        var zipFile = new ZipFile(filename);
        return new ReadResult(
                new PolygonsReader(getEntryStream(POLYGONS, zipFile)),
                new ObjectReader<>(getEntryStream(DIJKSTRA, zipFile)));
    }

    private static ObjectInputStream getEntryStream(String name, ZipFile zipFile) throws IOException {
        return new ObjectInputStream(zipFile.getInputStream(zipFile.getEntry(name)));
    }

    private static InputStream getInputStream(String filename) throws IOException {
        if (filename.endsWith(".zip")) {
            var zipFile = new ZipFile(filename);
            var entry = zipFile.entries().nextElement();
            return new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE);
        }

        return new BufferedInputStream(new FileInputStream(filename), BUFFER_SIZE);
    }
}
