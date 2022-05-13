package features;

import Search.AddressDatabase;
import io.*;
import navigation.Dijkstra;
import navigation.NearestNeighbor;

import java.io.ObjectInputStream;
import java.util.function.Function;

public enum Feature {
    DRAWING("Visuals", PolygonsReader::new),
    NEAREST_NEIGHBOR("Nearest Neighbor", ObjectReader<NearestNeighbor>::new),
    PATHFINDING("Pathfinding", ObjectReader<Dijkstra>::new),
    ADDRESS_SEARCH("Address Search", ObjectReader<AddressDatabase>::new);

    private final String displayName;
    private final Function<ObjectInputStream, Reader> reader;

    Feature(String displayName, Function<ObjectInputStream, Reader> reader) {
        this.displayName = displayName;
        this.reader = reader;
    }

    public Writer createWriter() {
        try {
            return switch (this) {
                case DRAWING -> new PolygonsWriter();
                case NEAREST_NEIGHBOR -> new ObjectWriter<>(new NearestNeighbor());
                case PATHFINDING -> new ObjectWriter<>(new Dijkstra());
                case ADDRESS_SEARCH -> new ObjectWriter<>(new AddressDatabase());
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Reader createReader(ObjectInputStream stream) {
        return reader.apply(stream);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
