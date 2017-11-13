package com.jj.algo;

/**
 * Created by donalys on 12/31/2016.
 */
final public class Fibonacci {
    private final long n;
    private long result;
    public Fibonacci(long n) {
        this.n = n;
        result = computeIterative(n);
        //result = computeRecursive(n);
    }

    public long get() {
        return result;
    }

    private static long computeIterative(long i) {
        if (i <= 1) return i;

        long minus1=1;
        long minus2=0;
        for (long j = 0; j < i-1; j++) {
            long next = minus1 + minus2;
            minus2 = minus1;
            minus1 = next;
        }

        return minus1;
    }

    private static long computeRecursive(long i) {
        if (i <= 1) return 1;
        return computeIterative(i-2) + computeIterative(i-1);
    }

    public static void main(String args[]) {
        System.out.printf("Fibonacci(%s)=%s %n", 50, new Fibonacci(50).get());
    }

}
