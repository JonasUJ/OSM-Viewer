package collections.lists;

import java.io.*;
import java.util.Arrays;

public class IntList implements Serializable {
    private int[] array = new int[8];
    private int n = 0;

    public IntList() {}

    public IntList(int[] array) {
        this.array = array;
        n = array.length;
    }

    public int add(int value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public int get(int index) {
        return array[index];
    }

    public int set(int index, int value) {
        return array[index] = value;
    }

    public int[] toArray() {
        return copyToSize(n);
    }

    public int[] getArray() {
        return array;
    }

    public int size() {
        return n;
    }

    public void truncate(int count) {
        n -= count;
    }

    void grow() {
        setSize(array.length * 2);
    }

    void setSize(int sz) {
        array = copyToSize(sz);
    }

    int[] copyToSize(int sz) {
        var tmp = new int[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(int value) {
        return Arrays.binarySearch(array, 0, n, value);
    }

    public void extend(IntList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize * 2);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
        n = newSize;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        array = (int[]) in.readUnshared();
        n = array.length;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUnshared(toArray());
    }
}
