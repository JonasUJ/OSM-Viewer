// Blatantly copied from https://algs4.cs.princeton.edu/23quicksort/Quick.java.html, stripped of
// everything but QuickSelect, and modified to use Lists instead of arrays.

package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code Quick} class provides static methods for sorting an array and selecting the ith
 * smallest element in an array using quicksort.
 *
 * <p>For additional documentation, see <a href="https://algs4.cs.princeton.edu/23quicksort">Section
 * 2.3</a> of <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class Quick {
    private Quick() {}

    // partition the subarray a[lo..hi] so that a[lo..j-1] <= a[j] <= a[j+1..hi]
    // and return the index j.
    private static <E> int partition(List<E> a, int lo, int hi, Comparator<E> cmp) {
        int i = lo;
        int j = hi + 1;
        E v = a.get(lo);
        while (true) {

            // find item on lo to swap
            while (less(a.get(++i), v, cmp)) {
                if (i == hi) break;
            }

            // find item on hi to swap
            while (less(v, a.get(--j), cmp)) {
                if (j == lo) break; // redundant since a[lo] acts as sentinel
            }

            // check if pointers cross
            if (i >= j) break;

            exch(a, i, j);
        }

        // put partitioning item v at a[j]
        exch(a, lo, j);

        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]
        return j;
    }

    /**
     * Rearranges the array so that {@code a[k]} contains the kth smallest key; {@code a[0]} through
     * {@code a[k-1]} are less than (or equal to) {@code a[k]}; and {@code a[k+1]} through {@code
     * a[n-1]} are greater than (or equal to) {@code a[k]}.
     *
     * @param a the array
     * @param k the rank of the key
     * @return the key of rank {@code k}
     * @throws IllegalArgumentException unless {@code 0 <= k < a.length}
     */
    public static <E> E select(List<E> a, int k, Comparator<E> cmp) {
        if (k < 0 || k >= a.size()) {
            throw new IllegalArgumentException("index is not between 0 and " + a.size() + ": " + k);
        }
        Collections.shuffle(a);
        int lo = 0, hi = a.size() - 1;
        while (hi > lo) {
            int i = partition(a, lo, hi, cmp);
            if (i > k) hi = i - 1;
            else if (i < k) lo = i + 1;
            else return a.get(i);
        }
        return a.get(lo);
    }

    // is v < w ?
    private static <E> boolean less(E v, E w, Comparator<E> cmp) {
        if (v == w) return false; // optimization when reference equals
        return cmp.compare(v, w) < 0;
    }

    // exchange a[i] and a[j]
    private static <E> void exch(List<E> a, int i, int j) {
        Collections.swap(a, i, j);
    }

    // partition the subarray a[lo..hi] so that a[lo..j-1] <= a[j] <= a[j+1..hi]
    // and return the index j.
    private static <E extends Comparable<? super E>> int partition(List<E> a, int lo, int hi) {
        int i = lo;
        int j = hi + 1;
        E v = a.get(lo);
        while (true) {

            // find item on lo to swap
            while (less(a.get(++i), v)) {
                if (i == hi) break;
            }

            // find item on hi to swap
            while (less(v, a.get(--j))) {
                if (j == lo) break; // redundant since a[lo] acts as sentinel
            }

            // check if pointers cross
            if (i >= j) break;

            exch(a, i, j);
        }

        // put partitioning item v at a[j]
        exch(a, lo, j);

        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]
        return j;
    }

    /**
     * Rearranges the array so that {@code a[k]} contains the kth smallest key; {@code a[0]} through
     * {@code a[k-1]} are less than (or equal to) {@code a[k]}; and {@code a[k+1]} through {@code
     * a[n-1]} are greater than (or equal to) {@code a[k]}.
     *
     * @param a the array
     * @param k the rank of the key
     * @return the key of rank {@code k}
     * @throws IllegalArgumentException unless {@code 0 <= k < a.length}
     */
    public static <E extends Comparable<? super E>> E select(List<E> a, int k) {
        if (k < 0 || k >= a.size()) {
            throw new IllegalArgumentException("index is not between 0 and " + a.size() + ": " + k);
        }
        Collections.shuffle(a);
        int lo = 0, hi = a.size() - 1;
        while (hi > lo) {
            int i = partition(a, lo, hi);
            if (i > k) hi = i - 1;
            else if (i < k) lo = i + 1;
            else return a.get(i);
        }
        return a.get(lo);
    }

    // is v < w ?
    private static <E extends Comparable<? super E>> boolean less(E v, E w) {
        if (v == w) return false; // optimization when reference equals
        return v.compareTo(w) < 0;
    }
}
