package io;

import features.Feature;
import features.FeatureSet;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;
import org.anarres.parallelgzip.ParallelGZIPInputStream;
import org.anarres.parallelgzip.ParallelGZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.IOUtils;
import osm.OSMObserver;
import osm.OSMReader;
import osm.elements.OSMBounds;

// TODO: Use a custom exception type instead of RuntimeException for when parsing fails.
public class FileParser implements IOConstants {
    public static final String EXT = ".map";
    private static final String FEATURES = "FEATURES";
    private static final String BOUNDS = "BOUNDS";

    public static File createMapFromOsm(
            File infile, FeatureSet features, ProgressBar bar, OSMObserver... observers)
            throws Exception {
        var writers = new ArrayList<Pair<String, Writer>>();

        { // `reader` gets its own scope so that it'll actually get GC'd at the end.
            // `reader = null` on its own just got optimized out.

            var reader = new OSMReader();

            writers.add(new Pair<>(FEATURES, new ObjectWriter<>(features)));
            writers.add(new Pair<>(BOUNDS, new ObjectWriter<>(new OSMBounds())));

            for (var feature : features) {
                writers.add(new Pair<>(feature.name(), feature.createWriter()));
            }

            for (var writer : writers) {
                reader.addObservers(writer.getValue());
            }

            reader.addObservers(observers);

            reader.parse(getInputStream(infile, bar));
        }

        System.gc();

        var outfile =
                new File(
                        infile.getPath().substring(0, infile.getPath().length() - infile.getName().length())
                                + infile.getName().split("\\.")[0]
                                + EXT);

        if (bar != null) Platform.runLater(() -> bar.setProgress(-1));

        createMapFromWriters(outfile, writers);

        System.gc();

        return outfile;
    }

    private static void createMapFromWriters(File outfile, List<Pair<String, Writer>> pairs)
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

    public static ReadResult readMap(File file) throws IOException {
        var tarFile = new TarFile(file);
        var readers = new HashMap<Feature, Reader>();
        var features = new ObjectReader<FeatureSet>(getEntryStream(FEATURES, tarFile)).read();
        for (var feature : features) {
            readers.put(feature, feature.createReader(getEntryStream(feature.name(), tarFile)));
        }
        return new ReadResult(
                readers, new ObjectReader<OSMBounds>(getEntryStream(BOUNDS, tarFile)).read());
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

    private static InputStream getInputStream(File file, ProgressBar bar) throws IOException {
        long size;
        InputStream stream;

        if (file.getName().endsWith(".zip")) {
            var zipFile = new ZipFile(file);
            var entry = zipFile.entries().nextElement();
            size = entry.getSize();
            stream = zipFile.getInputStream(entry);
        } else {
            stream = new FileInputStream(file);
            size = file.length();
        }

        stream = new BufferedInputStream(stream, BUFFER_SIZE);

        if (bar != null) stream = new ProgressBarInputStream(stream, bar, size);

        return stream;
    }
}
