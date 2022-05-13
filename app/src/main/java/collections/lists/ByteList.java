package collections.lists;

import java.io.*;
import java.util.Arrays;

public class ByteList implements Serializable {
    private byte[] array = new byte[8];
    private int n = 0;

    public ByteList() {
    }

    public ByteList(byte[] array) {
        this.array = array;
        n = array.length;
    }

    public int add(byte value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public byte get(int index) {
        return array[index];
    }

    public byte set(int index, byte value) {
        return array[index] = value;
    }

    public byte[] toArray() {
        return copyToSize(n);
    }

    public byte[] getArray() {
        return array;
    }

    public int size() {
        return n;
    }

    public void truncate(int count) {
        n -= Math.min(n, count);
    }

    public void limit(int count) {
        n -= Math.min(n, n - count);
    }

    void grow() {
        setSize(array.length * 2);
    }

    void setSize(int sz) {
        array = copyToSize(sz);
    }

    byte[] copyToSize(int sz) {
        var tmp = new byte[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(byte value) {
        return Arrays.binarySearch(array, 0, n, value);
    }

    public void extend(ByteList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize * 2);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
        n = newSize;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        array = (byte[]) in.readUnshared();
        n = array.length;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUnshared(toArray());
    }
}
