package collections.trie;

import java.util.Iterator;
import java.util.Map.Entry;

/** Trie */
public interface Trie<Value> {
    /**
     * Get a trie of all entries with the given prefix. The new trie is unaware of the prefix, meaning
     * queries to the new trie should not pass have this prefix.
     *
     * @param prefix Prefix to search for
     * @return A new trie where all the entries are the entries from this trie and with the given
     *     prefix, just with the prefix stripped.
     */
    Trie<Value> narrow(String prefix);

    /**
     * Get an iterator of all entries in the Trie with the given prefix
     *
     * @param prefix The prefix of the keys to search for
     * @return An iterator of all entries with the given prefix.
     */
    Iterator<Entry<String, Value>> withPrefix(String prefix);

    /**
     * Query whether the given key exists in the Trie
     *
     * @param key The exact key to query for
     * @return Whether an entry with the given key exists in the Trie
     */
    boolean contains(String key);

    /**
     * Get the value associated with a key
     *
     * @param key Key of the value to find
     * @return The value associated with the key
     */
    Value get(String key);
}
