package collections.lru;

class NodeList<K, V> {
    private Node<K, V> front;
    private Node<K, V> back;
    private int size;

    public void clear() {
        size = 0;
        front = null;
        back = null;
    }

    public void remove(Node<K, V> node) {
        // Remove from position in list

        // node.left will never be null - the last element is never removed because LRUCache disallows
        // capacities less than 1
        node.left.right = node.right;

        if (node.right == null) {
            back = node.left;
        } else {
            node.right.left = node.left;
        }

        size--; // good thing this is package-private, and we don't have to do all sorts of checks...
    }

    public void add(Node<K, V> node) {
        // Add to front
        if (front != null) front.left = node;
        if (back == null) back = node;
        node.right = front;
        node.left = null;
        front = node;
        size++;
    }

    public void touch(Node<K, V> node) {
        if (node == front) return;
        remove(node);
        add(node);
    }

    public int size() {
        return size;
    }

    public Node<K, V> pop() {
        var node = back;
        remove(node);
        return node;
    }
}
