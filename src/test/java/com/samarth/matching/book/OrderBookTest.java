package com.samarth.matching.book;

import com.samarth.matching.model.Order;
import com.samarth.matching.model.OrderType;
import com.samarth.matching.model.Side;
import com.samarth.matching.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    private OrderBook book;
    private long nextId;

    @BeforeEach
    void setUp() {
        book = new OrderBook("TEST");
        nextId = 1;
    }

    private Order limit(Side side, long price, int qty) {
        return new Order(nextId++, "TEST", side, OrderType.LIMIT, price, qty);
    }

    private Order market(Side side, int qty) {
        return new Order(nextId++, "TEST", side, OrderType.MARKET, 0, qty);
    }

    @Test
    void ordersThatDoNotCrossRestInBook() {
        assertTrue(book.process(limit(Side.BUY, 100, 10)).isEmpty());
        assertTrue(book.process(limit(Side.SELL, 105, 10)).isEmpty());
        assertEquals(100L, book.getBestBid());
        assertEquals(105L, book.getBestAsk());
    }

    @Test
    void matchingBuyTradesAtRestingSellPrice() {
        book.process(limit(Side.SELL, 100, 10));                   // order #1
        List<Trade> trades = book.process(limit(Side.BUY, 105, 10)); // order #2

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertEquals(100, trade.price());
        assertEquals(10, trade.quantity());
        assertEquals(2, trade.buyOrderId());
        assertEquals(1, trade.sellOrderId());
        assertTrue(book.isEmpty());
    }

    @Test
    void partialFillLeavesRemainderInBook() {
        book.process(limit(Side.SELL, 100, 10));
        List<Trade> trades = book.process(limit(Side.BUY, 100, 4));

        assertEquals(4, trades.get(0).quantity());
        assertEquals(6, book.getQuantityAt(Side.SELL, 100));
    }

    @Test
    void earlierOrderAtSamePriceIsFilledFirst() {
        book.process(limit(Side.SELL, 100, 5)); // order #1
        book.process(limit(Side.SELL, 100, 5)); // order #2
        List<Trade> trades = book.process(limit(Side.BUY, 100, 5));

        assertEquals(1, trades.get(0).sellOrderId());
        assertEquals(5, book.getQuantityAt(Side.SELL, 100));
    }

    @Test
    void buyOrderSweepsMultiplePriceLevels() {
        book.process(limit(Side.SELL, 100, 5));
        book.process(limit(Side.SELL, 101, 5));
        book.process(limit(Side.SELL, 102, 5));
        List<Trade> trades = book.process(limit(Side.BUY, 101, 12));

        assertEquals(2, trades.size());
        assertEquals(100, trades.get(0).price());
        assertEquals(101, trades.get(1).price());
        assertEquals(101L, book.getBestBid());   // the 2 unfilled shares now wait as a bid
        assertEquals(102L, book.getBestAsk());
    }

    @Test
    void limitOrderDoesNotTradeAtWorsePrice() {
        book.process(limit(Side.SELL, 105, 10));
        assertTrue(book.process(limit(Side.BUY, 100, 10)).isEmpty());
    }

    @Test
    void unfilledPartOfMarketOrderIsDiscarded() {
        book.process(limit(Side.SELL, 100, 3));
        List<Trade> trades = book.process(market(Side.BUY, 10));

        assertEquals(3, trades.get(0).quantity());
        assertTrue(book.isEmpty());   // the other 7 shares do not wait in the book
    }

    @Test
    void marketOrderOnEmptyBookDoesNothing() {
        assertTrue(book.process(market(Side.SELL, 10)).isEmpty());
        assertTrue(book.isEmpty());
    }

    @Test
    void cancelRemovesRestingOrder() {
        Order order = limit(Side.BUY, 100, 10);
        book.process(order);

        assertTrue(book.cancel(order.getId()));
        assertNull(book.getBestBid());
        assertFalse(book.cancel(order.getId()));   // cancelling twice fails
    }

    @Test
    void cannotCancelFullyFilledOrder() {
        book.process(limit(Side.SELL, 100, 5));   // order #1
        book.process(limit(Side.BUY, 100, 5));
        assertFalse(book.cancel(1));
    }
}