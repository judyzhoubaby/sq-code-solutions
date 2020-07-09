package datastructure.sparsetable;

import java.util.function.BinaryOperator;

/**
 * @author sqzhang
 * @year 2020
 */
public class SparseTable {

    // Fast log base 2 logarithm lookup table for i, 1 <= i <= n
    private int[] log2;
    // The sparse table values.
    private long[][] dp;
    // Index Table (IT) associated with the values in the sparse table.
    private int[][] it;

    // The various supported query operations on this sparse table.
    public enum Operation {
        MIN, MAX, SUM, MULTI, GCD
    }

    private Operation op;

    // All functions must be associative, e.g: a * (b * c) = (a * b) * c for some operation '*'
    private BinaryOperator<Long> sumFn = Long::sum;
    private BinaryOperator<Long> minFn = Math::min;
    private BinaryOperator<Long> maxFn = Math::max;
    private BinaryOperator<Long> multiFn = (a, b) -> a * b;
    private BinaryOperator<Long> gcdFn =
        (a, b) -> {
            long gcd = a;
            while (b != 0) {
                gcd = b;
                b = a % b;
                a = gcd;
            }
            return Math.abs(gcd);
        };

    public SparseTable(long[] v, Operation op) {
        this.op = op;
        // The number of elements in the original input array.
        int n = v.length;

        // Tip: to get the floor of the logarithm base 2 in Java you can also do:
        // Integer.numberOfTrailingZeros(Integer.highestOneBit(n)).
        // The maximum power of 2 needed. This value is floor(log2(n))
        int p = (int) (Math.log(n) / Math.log(2));
        dp = new long[p + 1][n];
        it = new int[p + 1][n];

        for (int i = 0; i < n; i++) {
            dp[0][i] = v[i];
            it[0][i] = i;
        }

        log2 = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            log2[i] = log2[i / 2] + 1;
        }

        // Build sparse table combining the values of the previous intervals.
        for (int i = 1; i <= p; i++) {
            for (int j = 0; j + (1 << i) <= n; j++) {
                long leftInterval = dp[i - 1][j];
                long rightInterval = dp[i - 1][j + (1 << (i - 1))];
                if (op == Operation.MIN) {
                    dp[i][j] = minFn.apply(leftInterval, rightInterval);
                    // Propagate the index of the best value
                    if (leftInterval <= rightInterval) {
                        it[i][j] = it[i - 1][j];
                    } else {
                        it[i][j] = it[i - 1][j + (1 << (i - 1))];
                    }
                } else if (op == Operation.MAX) {
                    dp[i][j] = maxFn.apply(leftInterval, rightInterval);
                    // Propagate the index of the best value
                    if (leftInterval >= rightInterval) {
                        it[i][j] = it[i - 1][j];
                    } else {
                        it[i][j] = it[i - 1][j + (1 << (i - 1))];
                    }
                } else if (op == Operation.SUM) {
                    dp[i][j] = sumFn.apply(leftInterval, rightInterval);
                } else if (op == Operation.MULTI) {
                    dp[i][j] = multiFn.apply(leftInterval, rightInterval);
                } else if (op == Operation.GCD) {
                    dp[i][j] = gcdFn.apply(leftInterval, rightInterval);
                }
            }
        }
    }

    // For debugging, testing and slides.
    private void printTable() {
        for (long[] r : dp) {
            for (long l : r) {
                System.out.printf("%02d, ", l);
            }
            System.out.println();
        }
    }

    // Queries [l, r] for the operation set on this sparse table.
    public long query(int l, int r) {
        // Fast queries types, O(1)
        if (op == Operation.MIN) {
            return query(l, r, minFn);
        } else if (op == Operation.MAX) {
            return query(l, r, maxFn);
        } else if (op == Operation.GCD) {
            return query(l, r, gcdFn);
        }

        // Slower query types, O(log2(n))
        if (op == Operation.SUM) {
            return sumQuery(l, r);
        } else {
            return multQuery(l, r);
        }
    }

    public int queryIndex(int l, int r) {
        if (op == Operation.MIN) {
            return minQueryIndex(l, r);
        } else if (op == Operation.MAX) {
            return maxQueryIndex(l, r);
        }
        throw new UnsupportedOperationException(
            "Operation type: " + op + " doesn't support index queries :/");
    }

    private int minQueryIndex(int l, int r) {
        int len = r - l + 1;
        int p = log2[len];
        long leftInterval = dp[p][l];
        long rightInterval = dp[p][r - (1 << p) + 1];
        if (leftInterval <= rightInterval) {
            return it[p][l];
        } else {
            return it[p][r - (1 << p) + 1];
        }
    }

    private int maxQueryIndex(int l, int r) {
        int len = r - l + 1;
        int p = log2[len];
        long leftInterval = dp[p][l];
        long rightInterval = dp[p][r - (1 << p) + 1];
        if (leftInterval >= rightInterval) {
            return it[p][l];
        } else {
            return it[p][r - (1 << p) + 1];
        }
    }

    // Do sum query [l, r] in O(log2(n)).
    //
    // Perform a cascading query which shrinks the left endpoint while summing over all the intervals
    // which are powers of 2 between [l, r].
    //
    // WARNING: This method can easily produces values that overflow.
    //
    // NOTE: You can achieve a faster time complexity and use less memory with a simple prefix sum
    // array. This method is here more as a proof of concept than for its usefulness.
    private long sumQuery(int l, int r) {
        long sum = 0;
        for (int p = log2[r - l + 1]; l <= r; p = log2[r - l + 1]) {
            sum += dp[p][l];
            l += (1 << p);
        }
        return sum;
    }

    private long multQuery(int l, int r) {
        long result = 1;
        for (int p = log2[r - l + 1]; l <= r; p = log2[r - l + 1]) {
            result *= dp[p][l];
            l += (1 << p);
        }
        return result;
    }

    // Do either a min, max or gcd query on the interval [l, r] in O(1).
    //
    // We can get O(1) query by finding the smallest power of 2 that fits within the interval length
    // which we'll call k. Then we can query the intervals [l, l+k] and [r-k+1, r] (which likely
    // overlap) and apply the function again. Some functions (like min and max) don't care about
    // overlapping intervals so this trick works, but for a function like sum this would return the
    // wrong result since it is not an idempotent binary function.
    private long query(int l, int r, BinaryOperator<Long> fn) {
        int len = r - l + 1;
        int p = log2[len];
        return fn.apply(dp[p][l], dp[p][r - (1 << p) + 1]);
    }

    /* Example usage: */

    public static void main(String[] args) {
        // example1();
        // example2();
        example3();
    }

    private static void example1() {
        long[] values = {1, 2, -3, 2, 4, -1, 5};

        // Initialize sparse table to do range minimum queries.
        SparseTable sparseTable = new SparseTable(values, SparseTable.Operation.MULTI);

        System.out.println(sparseTable.query(2, 3));
    }

    private static void exampleFromSlides() {
        long[] values = {4, 2, 3, 7, 1, 5, 3, 3, 9, 6, 7, -1, 4};

        // Initialize sparse table to do range minimum queries.
        SparseTable sparseTable = new SparseTable(values, SparseTable.Operation.MIN);

        System.out.printf("Min value between [2, 7] = %d\n", sparseTable.query(2, 7));
    }

    private static void example3() {
        long[] values = {4, 4, 4, 4, 4, 4};
        // Initialize sparse table to do range minimum queries.
        SparseTable sparseTable = new SparseTable(values, SparseTable.Operation.SUM);

        System.out.printf("%d\n", sparseTable.query(0, values.length - 1));
    }
}