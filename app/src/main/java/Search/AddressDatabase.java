package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;


import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


public class AddressDatabase implements OSMObserver, Serializable {
    private Trie<Set<AddressBuilder>> streetToAddress;
    private Trie<Set<AddressBuilder>> cityToAddress;
    private Trie<Set<AddressBuilder>> postcodeToAddress;
    private TrieBuilder<Set<AddressBuilder>> streetTrieBuilder;
    private TrieBuilder<Set<AddressBuilder>> cityTrieBuilder;
    private TrieBuilder<Set<AddressBuilder>> postcodeTrieBuilder;
    private List<Address> history;

    public AddressDatabase() {
        streetTrieBuilder = new TrieBuilder<>('\0');
        cityTrieBuilder = new TrieBuilder<>('\0');
        postcodeTrieBuilder = new TrieBuilder<>('\0');
        history = new ArrayList<>();
    }

    public static AddressBuilder parse(String toParse){
        var builder = new AddressBuilder();
        var matcher = PATTERN.matcher(toParse);

        if(matcher.matches()){
            builder.street(matcher.group("street"));
            builder.house(matcher.group("house"));
            builder.postcode(matcher.group("postcode"));
            builder.city(matcher.group("city"));
            builder.floor(matcher.group("floor"));
            builder.side(matcher.group("side"));
        } else{
            return null;
        }
        return builder;
    }

    public List<Address> getHistory(){
        return history;
    }

    public void addAddress(AddressBuilder a){
        addToTrie(streetTrieBuilder, a.getStreet(), a);
        addToTrie(cityTrieBuilder, a.getCity(), a);
        addToTrie(postcodeTrieBuilder, a.getPostcode(), a);
    }

    private void addToTrie(TrieBuilder<Set<AddressBuilder>> trie, String key, AddressBuilder value){
        var set = trie.get(key);
        if(set == null){
            set = new HashSet<>();
        }
        set.add(value);
        trie.put(key, set);
    }

    @Override
    public void onNode(OSMNode node){
        var builder = new AddressBuilder();

        boolean isAddress = false;

        for (OSMTag t : node.tags()){
            if (t.key()==OSMTag.Key.STREET){
                builder.street(t.value());
                isAddress=true;
            }
            if (t.key()==OSMTag.Key.HOUSENUMBER){
                builder.house(t.value());
            }
            if (t.key()==OSMTag.Key.CITY){
                builder.city(t.value());
            }
            if (t.key()==OSMTag.Key.POSTCODE){
                builder.postcode(t.value());
            }
        }

        builder.SlimOSMNode(node.slim());
        if (isAddress) addAddress(builder);
    }

    public void buildTries(){
        streetToAddress = streetTrieBuilder.build();
        cityToAddress = cityTrieBuilder.build();
        postcodeToAddress = postcodeTrieBuilder.build();
        postcodeTrieBuilder = null;
        streetTrieBuilder = null;
        cityTrieBuilder = null;
    }

    private final static String REGEX = "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public List<Address> search(AddressBuilder input){
        return null;
    }

    public List<Address> autofillSearch(AddressBuilder input, int maxEntries) {
        var inputAddress = input.build();
        final LinkedList<Address> results = new LinkedList<>();
        var parsedStreetsAndCities = new HashSet<String>();
        var filteringSet = new HashSet<AddressBuilder>();


        if(inputAddress.street() != null){
            var searchedStreets = streetToAddress.withPrefix(inputAddress.street());
            while(searchedStreets.hasNext()){
                filteringSet.addAll(searchedStreets.next().getValue());
            }
        }else {
            throw new RuntimeException();
            //Street can never be null due to the regex. If the regex doesn't match, this method is never called.
        }

        if(inputAddress.houseNumber() !=null){
            //we know that we must have a full street name, and since there probably isn't that many streets with the
            // same address it shouldn't take long to go through them linearly, to check if they have the specified house number



            var set = new HashSet<AddressBuilder>();

            filteringSet.stream().filter(e -> e.getHouse().startsWith(inputAddress.houseNumber())).forEach(set::add);
            filteringSet = set;
        }



        if(inputAddress.postcode() != null){
            var searchedPostcodes = postcodeToAddress.withPrefix(inputAddress.postcode());

            var retain = new HashSet<AddressBuilder>();
            while(searchedPostcodes.hasNext()){
                var entry = searchedPostcodes.next().getValue();
                retain.addAll(entry);
            }

            filteringSet.retainAll(retain);
        }

        if(inputAddress.city() != null){
            var searchedCities = cityToAddress.withPrefix(inputAddress.city());
            var retain = new HashSet<AddressBuilder>();
            while(searchedCities.hasNext()){
                var entry = searchedCities.next().getValue();
                retain.addAll(entry);
            }
            filteringSet.retainAll(retain);
        }


        filteringSet.stream().limit(maxEntries).forEach(e -> {
            if(parsedStreetsAndCities.add(e.getStreet() + "|" + e.getPostcode())){
                results.add(e.build());
            }
        });

        return results;



        /*

        if(inputAddress.city() != null){
            filterStream.filter(e -> e.getCity().startsWith(inputAddress.city()));
        }
        if(inputAddress.postcode() != null){
            filterStream.filter(e -> e.getPostcode().startsWith((inputAddress.postcode())));
        }

         */
    }


    //test method
    public void display(){
        for (Iterator<Entry<String, Set<AddressBuilder>>> it = streetToAddress.withPrefix(""); it.hasNext(); ) {
            it.next().getValue().forEach(e -> System.out.println(e.getStreet()));
        }
    }



}