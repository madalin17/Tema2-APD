package fibo;

import java.util.ArrayList;
import java.util.List;

public final class Fibonacci {
    private static Fibonacci fibonacci = null;
    public final List<Integer> sequence; /* list where fibo(i) is the i-th number in the Fibbonacci sequence */

    /**
     * Default constructor for a Fibbonacci sequence, adding fibo(0) and fibo(1)
     */
    private Fibonacci() {
        this.sequence = new ArrayList<>();
        sequence.add(0); /* fibo(0) */
        sequence.add(1); /* fibo(1) */
    }

    public static Fibonacci getFibonacci() {
        if (fibonacci == null) {
            fibonacci = new Fibonacci();
        }
        return fibonacci;
    }

    /**
     * Function calculates the n-th Fibbonacci number and all Fibbonacci numbers before it
     * if they are unknown yet
     * @param n an integer to calculate fibo for
     * @return the n-th number in the Fibbonacci sequence
     */
    public int compute(int n) {
        if (n <= 0) { /* make sure n is positive */
            return 0;
        }

        int size = sequence.size();
        while (size < n + 1) { /* calculate all Fibbonacci numbers nonexistent in the list, till the n-th one */
            sequence.add(sequence.get(size - 1) + sequence.get(size - 2));
            size++;
        }
        return sequence.get(n);
    }
}