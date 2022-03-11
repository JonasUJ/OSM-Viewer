package collections.trie;

import java.util.ArrayList;

/**
 * TrieBuilder is an interface for creating FinalTries.
 *
 * @param <Value> The type that can be looked up using String keys.
 */
public class TrieBuilder<Value> {
    private final char key;
    private Value value;
    private final ArrayList<TrieBuilder<Value>> children = new ArrayList<>();

    public TrieBuilder(char key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public FinalTrie<Value> build() {
        var trie = new FinalTrie<Value>(key, value, new FinalTrie[children.size()]);

        for (int i = 0; i < children.size(); i++) {
            trie.children[i] = children.get(i).build();
        }

        return trie;
    }

    public void put(String key, Value value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");

        put(key, 0, value);
    }

    private void put(String key, int idx, Value value) {
        var child = getChild(key.charAt(idx));

        if (child == null) {
            child = new TrieBuilder<Value>(key.charAt(idx));
            children.add(child);
        }

        if (idx == key.length() - 1) {
            child.value = value;
        } else {
            child.put(key, idx + 1, value);
        }
    }

    private TrieBuilder<Value> getChild(char c) {
        for (var child : children) {
            if (child.key == c) {
                return child;
            }
        }

        return null;
    }
}
