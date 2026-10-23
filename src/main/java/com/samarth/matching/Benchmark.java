package com.samarth.matching;

import com.samarth.matching.engine.MatchingEngine;
import com.samarth.matching.model.Side;

import java.util.Random;

public class Benchmark {
    private static final int WARMUP_ORDERS = 200_000;
    private static final int MEASURED_ORDERS = 1_000_000;

    public static void main(String[] args) {
        run(WARMUP_ORDERS, false);    // a practice run so Java can optimise the code first
        run(MEASURED_ORDERS, true);   // the real measurement
    }

    private static void run(int count, boolean report) {
        Random random = new Random(42);   // fixed seed, so every run uses the same orders
        Side[] sides = new Side[count];
        long[] prices = new long[count];
        int[] quantities = new int[count];

        for (int i = 0; i < count; i++) {
            sides[i] = random.nextBoolean() ? Side.BUY : Side.SELL;
            prices[i] = 50_000 + (random.nextInt(41) - 20) * 5L;   // between Rs.499.00 and Rs.501.00
            quantities[i] = 1 + random.nextInt(100);
        }

        MatchingEngine engine = new MatchingEngine(false);
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            engine.placeLimitOrder("BENCH", sides[i], prices[i], quantities[i]);
        }
        long elapsed = System.nanoTime() - start;

        if (report) {
            double seconds = elapsed / 1_000_000_000.0;
            System.out.printf("Processed %,d orders in %.2f seconds%n", count, seconds);
            System.out.printf("Throughput: %,.0f orders/second%n", count / seconds);
            System.out.printf("Trades executed: %,d%n", engine.getTradeLog().size());
        }
    }
}