package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;
import util.Predicates;

public class AddressDatabase implements OSMObserver, Serializable {
    private Trie<List<AddressBuilder>> streetToAddress;
    private Trie<List<AddressBuilder>> cityToAddress;
    private Trie<List<AddressBuilder>> postcodeToAddress;
    private TrieBuilder<List<AddressBuilder>> streetTrieBuilder;
    private TrieBuilder<List<AddressBuilder>> cityTrieBuilder;
    private TrieBuilder<List<AddressBuilder>> postcodeTrieBuilder;
    private List<Address> history;

    public AddressDatabase() {
        streetTrieBuilder = new TrieBuilder<>('\0');
        cityTrieBuilder = new TrieBuilder<>('\0');
        postcodeTrieBuilder = new TrieBuilder<>('\0');
        history = new ArrayList<>();
    }

    public static AddressBuilder parse(String toParse) {
        var builder = new AddressBuilder();
        var matcher = PATTERN.matcher(toParse);

        if (matcher.matches()) {
            builder.street(matcher.group("street"));
            builder.house(matcher.group("house"));
            builder.postcode(matcher.group("postcode"));
            builder.city(matcher.group("city"));
            builder.floor(matcher.group("floor"));
            builder.side(matcher.group("side"));
        } else {
            return null;
        }
        return builder;
    }

    public List<Address> getHistory() {
        return history;
    }

    public void addAddress(AddressBuilder a) {
        addToTrie(streetTrieBuilder, a.getStreet(), a);
        if (a.getCity() != null) {
            addToTrie(cityTrieBuilder, a.getCity(), a);
        }
        if (a.getPostcode() != null) {
            addToTrie(postcodeTrieBuilder, a.getPostcode(), a);
        }
    }

    private void addToTrie(TrieBuilder<List<AddressBuilder>> trie, String key, AddressBuilder value) {
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

        builder.SlimOSMNode(node.slim());
        if (isAddress) addAddress(builder);
    }

    public void buildTries() {
        streetToAddress = streetTrieBuilder.build();
        cityToAddress = cityTrieBuilder.build();
        postcodeToAddress = postcodeTrieBuilder.build();
        postcodeTrieBuilder = null;
        streetTrieBuilder = null;
        cityTrieBuilder = null;
    }

    private static final String REGEX =
            "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public List<Address> search(Address input) {

        var searchedStreets = streetToAddress.get(input.street());
        if (searchedStreets == null) return null;

        var filterStream = searchedStreets.stream();

        if (input.postcode() != null) {
            filterStream = filterStream.filter(e -> e.getPostcode().equals(input.postcode()));
        }
        if (input.city() != null) {
            filterStream = filterStream.filter(e -> e.getCity().equals(input.city()));
        }
        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.getHouse().equals(input.houseNumber()));
        }
        return filterStream.map(AddressBuilder::build).toList();
    }

    public List<Address> possibleAddresses(Address input, int maxEntries) {
        Stream<AddressBuilder> filterStream =
                StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        streetToAddress.withPrefix(input.street()), Spliterator.ORDERED),
                                false)
                        .flatMap(e -> e.getValue().stream());

        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.getHouse().startsWith(input.houseNumber()));
        }

        if (input.postcode() != null) {
            var searchedPostcodes = postcodeToAddress.withPrefix(input.postcode());

            var retain = new HashSet<AddressBuilder>();
            while (searchedPostcodes.hasNext()) {
                var entry = searchedPostcodes.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        if (input.city() != null) {
            var searchedCities = cityToAddress.withPrefix(input.city());
            var retain = new HashSet<AddressBuilder>();
            while (searchedCities.hasNext()) {
                var entry = searchedCities.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        if (input.houseNumber() != null) {
            return filterStream.limit(maxEntries).map(AddressBuilder::build).toList();
        } else {
            return filterStream
                    .filter(
                            Predicates.distinctByKey(e -> e.getStreet().hashCode() * e.getPostcode().hashCode()))
                    .limit(maxEntries)
                    .map(AddressBuilder::build)
                    .toList();
        }

        //        if (input.houseNumber() != null) {
        //            filteringSet.stream().limit(maxEntries).forEach(e -> results.add(e.build()));
        //        } else {
        //            filteringSet.stream()
        //                    .limit(maxEntries)
        //                    .forEach(
        //                            e -> {
        //                                if (parsedStreetsAndCities.add(e.getStreet() + "|" +
        // e.getPostcode())) {
        //                                    results.add(e.build());
        //                                }
        //                            });
        //        }
    }

    // test method
    public void display() {
        for (Iterator<Entry<String, List<AddressBuilder>>> it = streetToAddress.withPrefix("");
                it.hasNext(); ) {
            it.next().getValue().forEach(e -> System.out.println(e.getStreet()));
        }
    }
}
