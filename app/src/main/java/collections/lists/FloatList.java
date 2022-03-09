package collections.lists;

import java.io.Serializable;
import java.util.Arrays;

public class FloatList implements Serializable {
    float[] array = new float[1];
    int n = 0;

    public FloatList() {}

    public int add(float value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public float get(int index) {
        return array[index];
    }

    public float set(int index, float value) {
        return array[index] = value;
    }

    public float[] toArray() {
        return copyToSize(n);
    }

    public float[] getArray() {
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

    float[] copyToSize(int sz) {
        var tmp = new float[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(float value) {
        return Arrays.binarySearch(array, 0, n, value);
    }

    public void extend(FloatList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
    }
}
