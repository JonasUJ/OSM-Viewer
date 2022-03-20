package collections.trie;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

/** FinalTrie is a readonly Trie */
public class FinalTrie<Value> implements Trie<Value>, Iterable<Entry<String, Value>>, Serializable {
    final char key;
    final Value value;
    final FinalTrie<Value>[] children;

    FinalTrie(char key, Value value, FinalTrie<Value>[] children) {
        this.key = key;
        this.value = value;
        this.children = children;
    }

    public FinalTrie<Value> narrow(String prefix) {
        return find(prefix, 0);
    }

    public Iterator<Entry<String, Value>> withPrefix(String prefix) {
        var subtrie = narrow(prefix);

        if (subtrie != null) {
            return new TrieIterator<Value>(subtrie, prefix);
        } else {
            return Collections.emptyIterator();
        }
    }

    @Override
    public Iterator<Entry<String, Value>> iterator() {
        return withPrefix("");
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public Value get(String key) {
        var subtrie = find(key, 0);
        if (subtrie != null) {
            return subtrie.value;
        } else {
            return null;
        }
    }

    private FinalTrie<Value> find(String prefix, int idx) {
        if (idx == prefix.length()) return this;

        var child = getChild(prefix.charAt(idx));
        if (child == null) return null;

        return child.find(prefix, idx + 1);
    }

    private FinalTrie<Value> getChild(char c) {
        // Linear search is probably the fastest in this situation,
        // yet I don't know without benchamrking :)
        //
        // My hypothesis is that this will prove to be negligible
        // compared to the cost of all the indirections involved
        // in traversing the tree.
        for (var child : children) {
            if (child.key == c) return child;
        }

        return null;
    }
}
