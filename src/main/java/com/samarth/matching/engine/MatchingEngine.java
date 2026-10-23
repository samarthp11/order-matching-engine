package com.samarth.matching.engine;

import com.samarth.matching.book.OrderBook;
import com.samarth.matching.exception.InvalidOrderException;
import com.samarth.matching.model.Order;
import com.samarth.matching.model.OrderType;
import com.samarth.matching.model.Side;
import com.samarth.matching.model.Trade;

import java.util.*;

public class MatchingEngine {
    private final Map<String, OrderBook> books = new HashMap<>();
    private final List<Trade> tradeLog = new ArrayList<>();
    private long nextOrderId = 1;

    public long placeLimitOrder(String symbol, Side side, long price, int quantity) {
        validate(symbol, quantity);
        if (price <= 0) {
            throw new InvalidOrderException("Limit price must be positive");
        }
        Order order = new Order(nextOrderId++, symbol, side, OrderType.LIMIT, price, quantity);
        submit(order);
        return order.getId();
    }

    public long placeMarketOrder(String symbol, Side side, int quantity) {
        validate(symbol, quantity);
        Order order = new Order(nextOrderId++, symbol, side, OrderType.MARKET, 0, quantity);
        submit(order);
        return order.getId();
    }

    public boolean cancelOrder(String symbol, long orderId) {
        OrderBook book = books.get(symbol);
        return book != null && book.cancel(orderId);
    }

    public OrderBook getBook(String symbol) {
        return books.get(symbol);
    }

    public List<Trade> getTradeLog() {
        return Collections.unmodifiableList(tradeLog);
    }

    private void submit(Order order) {
        OrderBook book = books.computeIfAbsent(order.getSymbol(), OrderBook::new);
        List<Trade> trades = book.process(order);
        for (Trade t : trades) {
            System.out.println(t);
        }
        tradeLog.addAll(trades);
    }

    private void validate(String symbol, int quantity) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidOrderException("Symbol is required");
        }
        if (quantity <= 0) {
            throw new InvalidOrderException("Quantity must be positive");
        }
    }
}