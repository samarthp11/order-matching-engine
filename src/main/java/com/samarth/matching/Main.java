package com.samarth.matching;

import com.samarth.matching.engine.MatchingEngine;
import com.samarth.matching.exception.InvalidOrderException;
import com.samarth.matching.model.Side;

public class Main {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        // Sellers and one buyer place orders. None of them match yet.
        engine.placeLimitOrder("RELIANCE", Side.SELL, 50500, 100); // order #1: sell 100 @ Rs.505
        engine.placeLimitOrder("RELIANCE", Side.SELL, 50200, 50);  // order #2: sell 50 @ Rs.502
        engine.placeLimitOrder("RELIANCE", Side.SELL, 50200, 30);  // order #3: sell 30 @ Rs.502
        engine.placeLimitOrder("RELIANCE", Side.BUY, 49800, 70);   // order #4: buy 70 @ Rs.498
        engine.getBook("RELIANCE").print();

        System.out.println("\nBuyer wants 60 shares, will pay up to Rs.503:");
        engine.placeLimitOrder("RELIANCE", Side.BUY, 50300, 60);   // order #5

        System.out.println("\nMarket order: sell 40 shares at the best price:");
        engine.placeMarketOrder("RELIANCE", Side.SELL, 40);        // order #6

        System.out.println("\nCancelling order #1:");
        System.out.println("Cancelled: " + engine.cancelOrder("RELIANCE", 1));
        engine.getBook("RELIANCE").print();

        System.out.println("\nTrying an invalid order:");
        try {
            engine.placeLimitOrder("RELIANCE", Side.BUY, 50000, -5);
        } catch (InvalidOrderException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}