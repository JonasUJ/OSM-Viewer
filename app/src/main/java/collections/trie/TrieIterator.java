package collections.trie;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/** PrefixIterator iterates over keys with a given prefix. */
public class TrieIterator<Value> implements Iterator<Entry<String, Value>> {
    private final String prefix;
    private final StringBuilder rest = new StringBuilder();
    private final List<Iterator<FinalTrie<Value>>> branch = new ArrayList<>();

    TrieIterator(FinalTrie<Value> trie, String prefix) {
        this.prefix = prefix;
        branch.add(Arrays.stream(trie.children).iterator());
    }

    @Override
    public boolean hasNext() {
        if (branch.size() == 0) return false;

        var iter = branch.get(branch.size() - 1);

        // Ascend
        while (!iter.hasNext()) {
            branch.remove(branch.size() - 1);

            if (branch.size() == 0) {
                return false;
            }

            rest.deleteCharAt(rest.length() - 1);

            // Go one level up the branch
            iter = branch.get(branch.size() - 1);
        }

        return branch.size() > 0;
    }

    @Override
    public Entry<String, Value> next() {
        if (!hasNext()) throw new NoSuchElementException();

        var iter = branch.get(branch.size() - 1);

        // Descend
        while (iter.hasNext()) {
            var subtrie = iter.next();
            rest.append(subtrie.key);

            // Go one level down the branch
            iter = Arrays.stream(subtrie.children).iterator();
            branch.add(iter);

            if (subtrie.value != null) {
                return new SimpleEntry<String, Value>(prefix + rest, subtrie.value);
            }
        }

        // hasNext ascends until we find a node with children, or we exit early.
        // Finding a node with children guarantees that we'll find more elements,
        // since at least all leafs are elements.
        throw new AssertionError("Malformed trie");
    }
}
