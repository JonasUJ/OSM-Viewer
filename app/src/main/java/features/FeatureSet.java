package features;

import osm.OSMObserver;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class FeatureSet extends HashSet<Feature> implements OSMObserver, Serializable {
    public static final FeatureSet ALL = new FeatureSet(EnumSet.allOf(Feature.class));

    public FeatureSet() {
    }

    public FeatureSet(Set<Feature> set) {
        addAll(set);
    }
}
