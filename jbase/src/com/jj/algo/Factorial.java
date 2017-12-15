package com.jj.algo;

/**
 * Created by yejaz on 1/1/2017.
 */
public class Factorial {
    private final int n;
    private long result;
    public Factorial(int n) {
        this.n = n;
        result = compute(n);
    }

    public long get() {
        return result;
    }

    public static long compute(int n) {
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
