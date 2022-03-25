package io;

import Search.AddressDatabase;
import java.io.*;
import java.util.zip.ZipFile;
import javafx.util.Pair;
import javax.xml.stream.XMLStreamException;
import navigation.Dijkstra;
import org.anarres.parallelgzip.ParallelGZIPInputStream;
import org.anarres.parallelgzip.ParallelGZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.IOUtils;
import osm.OSMReader;

// TODO: Use a custom exception type instead of RuntimeException for when parsing fails.
public class FileParser implements IOConstants {
    private static final String EXT = ".gz.tar";
    private static final String POLYGONS = "polygons";
    private static final String DIJKSTRA = "dijkstra";
    private static final String ADDRESSES = "addresses";

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
        var addressWriter = new ObjectWriter<>(new AddressDatabase());
        reader.addObservers(polygonsWriter, dijkstraWriter, addressWriter);

        reader.parse(getInputStream(infile));

        var outfile = infile.split("\\.")[0] + EXT;
        createMapFromWriters(
                outfile,
                new Pair<>(POLYGONS, polygonsWriter),
                new Pair<>(DIJKSTRA, dijkstraWriter),
                new Pair<>(ADDRESSES, addressWriter));

        return outfile;
    }

    @SafeVarargs
    private static void createMapFromWriters(String outfile, Pair<String, Writer>... pairs)
            throws IOException {
        try (var tarStream =
                new TarArchiveOutputStream(
                        new BufferedOutputStream(new FileOutputStream(outfile), BUFFER_SIZE))) {
            for (var pair : pairs) {
                var file = writeToFile(pair.getValue());
                var entry = tarStream.createArchiveEntry(file, pair.getKey());
                tarStream.putArchiveEntry(entry);
                IOUtils.copy(file, tarStream);
                tarStream.closeArchiveEntry();
            }
        }
    }

    private static File writeToFile(Writer writer) throws IOException {
        var file = File.createTempFile("osm", "");
        file.deleteOnExit();
        try (var pigzStream = new ParallelGZIPOutputStream(new FileOutputStream(file))) {
            writer.writeTo(pigzStream);
            pigzStream.flush();
        }
        return file;
    }

    private static ReadResult readMap(String filename) throws IOException {
        var tarFile = new TarFile(new File(filename));
        return new ReadResult(
                new PolygonsReader(getEntryStream(POLYGONS, tarFile)),
                new ObjectReader<>(getEntryStream(DIJKSTRA, tarFile)),
                new ObjectReader<>(getEntryStream(ADDRESSES, tarFile)));
    }

    private static ObjectInputStream getEntryStream(String name, TarFile tarFile) throws IOException {
        return new ObjectInputStream(
                new ParallelGZIPInputStream(
                        tarFile.getInputStream(
                                tarFile.getEntries().stream()
                                        .filter(e -> e.getName().equals(name))
                                        .findFirst()
                                        .orElseThrow())));
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
