package com.samarth.matching.engine;

import com.samarth.matching.exception.InvalidOrderException;
import com.samarth.matching.model.Side;
import com.samarth.matching.model.Trade;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {
    private final MatchingEngine engine = new MatchingEngine(false);

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(InvalidOrderException.class, () -> engine.placeLimitOrder("TEST", Side.BUY, 100, 0));
        assertThrows(InvalidOrderException.class, () -> engine.placeMarketOrder("TEST", Side.SELL, -5));
    }

    @Test
    void rejectsNonPositiveLimitPrice() {
        assertThrows(InvalidOrderException.class, () -> engine.placeLimitOrder("TEST", Side.BUY, 0, 10));
    }

    @Test
    void rejectsMissingSymbol() {
        assertThrows(InvalidOrderException.class, () -> engine.placeLimitOrder(null, Side.BUY, 100, 10));
        assertThrows(InvalidOrderException.class, () -> engine.placeLimitOrder("  ", Side.BUY, 100, 10));
    }

    @Test
    void assignsIncreasingOrderIds() {
        long first = engine.placeLimitOrder("TEST", Side.BUY, 100, 10);
        long second = engine.placeLimitOrder("TEST", Side.BUY, 100, 10);
        assertEquals(first + 1, second);
    }

    @Test
    void differentSymbolsNeverMatch() {
        engine.placeLimitOrder("AAA", Side.SELL, 100, 10);
        engine.placeLimitOrder("BBB", Side.BUY, 100, 10);
        assertTrue(engine.getTradeLog().isEmpty());
    }

    @Test
    void tradesAreRecordedInLog() {
        engine.placeLimitOrder("TEST", Side.SELL, 100, 10);
        engine.placeLimitOrder("TEST", Side.BUY, 100, 10);
        assertEquals(1, engine.getTradeLog().size());
    }

    @Test
    void cancelOnUnknownSymbolReturnsFalse() {
        assertFalse(engine.cancelOrder("UNKNOWN", 1));
    }

    @Test
    void concurrentOrdersAreProcessedSafely() throws InterruptedException {
        int threadCount = 8;
        int ordersPerThread = 10_000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            Side side = (t % 2 == 0) ? Side.BUY : Side.SELL;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < ordersPerThread; i++) {
                    ids.add(engine.placeLimitOrder("TEST", side, 100, 1));
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            thread.join();   // wait for every thread to finish
        }

        assertEquals(threadCount * ordersPerThread, ids.size(), "every order must get a unique ID");

        int tradedQuantity = engine.getTradeLog().stream().mapToInt(Trade::quantity).sum();
        assertEquals((threadCount / 2) * ordersPerThread, tradedQuantity, "every share must be traded exactly once");
        assertTrue(engine.getBook("TEST").isEmpty(), "book must be empty at the end");
    }
}