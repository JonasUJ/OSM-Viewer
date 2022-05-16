package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import geometry.Point;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;
import pointsOfInterest.PointOfInterest;
import util.Predicates;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AddressDatabase implements OSMObserver, Serializable {

    private static final String REGEX =
            "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private Trie<List<Address>> streetToAddress;
    private Trie<List<Address>> cityToAddress;
    private Trie<List<Address>> postcodeToAddress;
    private TrieBuilder<List<Address>> streetTrieBuilder;
    private TrieBuilder<List<Address>> cityTrieBuilder;
    private TrieBuilder<List<Address>> postcodeTrieBuilder;
    private List<PointOfInterest> pointsOfInterest;
  
    public AddressDatabase() {
        streetTrieBuilder = new TrieBuilder<>('\0');
        cityTrieBuilder = new TrieBuilder<>('\0');
        postcodeTrieBuilder = new TrieBuilder<>('\0');
        pointsOfInterest = new ArrayList<>();

    }

    public static AddressBuilder parse(String toParse) {

        var builder = new AddressBuilder();
        if (toParse.contains("(Point of interest)")) {
            builder.street(toParse);
            builder.house("");
            builder.city("");
            builder.postcode("");
            return builder;
        }

        var matcher = PATTERN.matcher(toParse);

        if (matcher.matches()) {
            builder.street(matcher.group("street"));
            builder.house(matcher.group("house"));
            builder.postcode(matcher.group("postcode"));
            builder.city(matcher.group("city"));
        } else {
            return null;
        }
        return builder;
    }

    public void addAddress(Address a) {
        addToTrie(streetTrieBuilder, a.street(), a);
        if (a.city() != null) {
            addToTrie(cityTrieBuilder, a.city(), a);
        }
        if (a.postcode() != null) {
            addToTrie(postcodeTrieBuilder, a.postcode(), a);
        }
    }

    private void addToTrie(TrieBuilder<List<Address>> trie, String key, Address value) {
        var set = trie.get(key);
        if (set == null) {
            set = new ArrayList<>();
        }
        set.add(value);
        trie.put(key, set);
    }

    @Override
    public void onNode(OSMNode node) {
        var builder = new AddressBuilder();

        boolean isAddress = false;

        for (OSMTag t : node.tags()) {
            if (t.key() == OSMTag.Key.STREET) {
                builder.street(t.value());
                isAddress = true;
            }
            if (t.key() == OSMTag.Key.HOUSENUMBER) {
                builder.house(t.value());
            }
            if (t.key() == OSMTag.Key.CITY) {
                builder.city(t.value());
            }
            if (t.key() == OSMTag.Key.POSTCODE) {
                builder.postcode(t.value());
            }
        }

        builder.lat((float) node.lat());
        builder.lon((float) node.lon());

        if (isAddress) addAddress(builder.build());
    }

    @Override
    public void onFinish() {
        streetToAddress = streetTrieBuilder.build();
        cityToAddress = cityTrieBuilder.build();
        postcodeToAddress = postcodeTrieBuilder.build();
        postcodeTrieBuilder = null;
        streetTrieBuilder = null;
        cityTrieBuilder = null;
    }

    public List<Address> search(Address input) {

        for (PointOfInterest poi : pointsOfInterest) {
            if (input
                    .street()
                    .equalsIgnoreCase((poi.name() + " (point of interest)"))) {
                var list = new ArrayList<Address>();
                list.add(
                        new Address(
                                poi.name() + " (Point of interest)",
                                "",
                                "",
                                "",
                                (float) Point.mapToGeoY(poi.lat()),
                                (float) Point.mapToGeoX(poi.lon())));
                return list;
            }
        }

        var searchedStreets = streetToAddress.get(input.street());
        if (searchedStreets == null) return null;

        var filterStream = searchedStreets.stream();

        if (input.postcode() != null) {
            filterStream = filterStream.filter(e -> e.postcode().equals(input.postcode()));
        }
        if (input.city() != null) {
            filterStream = filterStream.filter(e -> e.city().equals(input.city()));
        }
        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.houseNumber().equals(input.houseNumber()));
        }
        return filterStream.toList();
    }

    public List<Address> possibleAddresses(Address input, int maxEntries) {
        Stream<Address> filterStream =
                StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        streetToAddress.withPrefix(input.street()), Spliterator.ORDERED),
                                false)
                        .flatMap(e -> e.getValue().stream());

        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.houseNumber().startsWith(input.houseNumber()));
        }

        if (input.postcode() != null) {
            var searchedPostcodes = postcodeToAddress.withPrefix(input.postcode());

            var retain = new HashSet<Address>();
            while (searchedPostcodes.hasNext()) {
                var entry = searchedPostcodes.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        if (input.city() != null) {
            var searchedCities = cityToAddress.withPrefix(input.city());
            var retain = new HashSet<Address>();
            while (searchedCities.hasNext()) {
                var entry = searchedCities.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        List<Address> results;
        if (input.houseNumber() != null) {
            results = filterStream.limit(maxEntries).toList();
        } else {
            results =
                    filterStream
                            .filter(
                                    Predicates.distinctByKey(e -> e.street().hashCode() * e.postcode().hashCode()))
                            .limit(maxEntries)
                            .toList();
        }

        List<Address> newResult = new ArrayList<>();
        for (PointOfInterest poi : pointsOfInterest) {
            if (poi.name().toLowerCase().startsWith(input.street().toLowerCase())) {
                newResult.add(
                        new Address(
                                poi.name() + " (Point of interest)",
                                "",
                                "",
                                "",
                                (float) Point.mapToGeoY(poi.lat()),
                                (float) Point.mapToGeoX(poi.lon())));
            }
        }

        newResult.addAll(results);

        return newResult;
    }

    public void setPointsOfInterest(List<PointOfInterest> pointsOfInterest) {
        this.pointsOfInterest = pointsOfInterest;

    }
}
