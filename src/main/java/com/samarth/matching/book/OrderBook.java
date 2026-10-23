package com.samarth.matching.book;

import com.samarth.matching.model.Order;
import com.samarth.matching.model.OrderBookSnapshot;
import com.samarth.matching.model.OrderType;
import com.samarth.matching.model.PriceLevel;
import com.samarth.matching.model.Side;
import com.samarth.matching.model.Trade;

import java.util.*;

public class OrderBook {
    private final String symbol;
    private final TreeMap<Long, ArrayDeque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, ArrayDeque<Order>> asks = new TreeMap<>();
    private final Map<Long, Order> ordersById = new HashMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public synchronized List<Trade> process(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<Long, ArrayDeque<Order>> opposite = (incoming.getSide() == Side.BUY) ? asks : bids;

        while (incoming.getRemainingQuantity() > 0 && !opposite.isEmpty()) {
            long bestPrice = opposite.firstKey();

            if (incoming.getType() == OrderType.LIMIT && !isPriceAcceptable(incoming, bestPrice)) {
                break;
            }

            ArrayDeque<Order> queue = opposite.get(bestPrice);
            Order resting = queue.peekFirst();

            int qty = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());
            incoming.fill(qty);
            resting.fill(qty);
            trades.add(createTrade(incoming, resting, bestPrice, qty));

            if (resting.isFilled()) {
                queue.pollFirst();
                ordersById.remove(resting.getId());
            }
            if (queue.isEmpty()) {
                opposite.remove(bestPrice);
            }
        }

        if (incoming.getRemainingQuantity() > 0 && incoming.getType() == OrderType.LIMIT) {
            addToBook(incoming);
        }
        return trades;
    }

    private boolean isPriceAcceptable(Order incoming, long bestPrice) {
        if (incoming.getSide() == Side.BUY) {
            return bestPrice <= incoming.getPrice();
        }
        return bestPrice >= incoming.getPrice();
    }

    private Trade createTrade(Order incoming, Order resting, long price, int qty) {
        long buyId = (incoming.getSide() == Side.BUY) ? incoming.getId() : resting.getId();
        long sellId = (incoming.getSide() == Side.SELL) ? incoming.getId() : resting.getId();
        return new Trade(symbol, buyId, sellId, price, qty);
    }

    private void addToBook(Order order) {
        TreeMap<Long, ArrayDeque<Order>> sideBook = (order.getSide() == Side.BUY) ? bids : asks;
        sideBook.computeIfAbsent(order.getPrice(), p -> new ArrayDeque<>()).addLast(order);
        ordersById.put(order.getId(), order);
    }

    public synchronized boolean cancel(long orderId) {
        Order order = ordersById.remove(orderId);
        if (order == null) {
            return false;
        }
        TreeMap<Long, ArrayDeque<Order>> sideBook = (order.getSide() == Side.BUY) ? bids : asks;
        ArrayDeque<Order> queue = sideBook.get(order.getPrice());
        queue.remove(order);
        if (queue.isEmpty()) {
            sideBook.remove(order.getPrice());
        }
        return true;
    }

    public synchronized Long getBestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    public synchronized Long getBestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    public synchronized int getQuantityAt(Side side, long price) {
        ArrayDeque<Order> queue = (side == Side.BUY) ? bids.get(price) : asks.get(price);
        return (queue == null) ? 0 : totalQuantity(queue);
    }

    public synchronized boolean isEmpty() {
        return bids.isEmpty() && asks.isEmpty();
    }

    public synchronized OrderBookSnapshot snapshot() {
        return new OrderBookSnapshot(symbol, toLevels(bids), toLevels(asks));
    }

    private List<PriceLevel> toLevels(TreeMap<Long, ArrayDeque<Order>> sideBook) {
        List<PriceLevel> levels = new ArrayList<>();
        sideBook.forEach((price, queue) -> levels.add(new PriceLevel(price, totalQuantity(queue))));
        return levels;
    }

    public synchronized void print() {
        System.out.println("------ Order Book: " + symbol + " ------");
        System.out.println("SELL orders (asks):");
        asks.descendingMap().forEach((price, queue) ->
                System.out.println("   " + Trade.formatPrice(price) + "  x " + totalQuantity(queue)));
        System.out.println("BUY orders (bids):");
        bids.forEach((price, queue) ->
                System.out.println("   " + Trade.formatPrice(price) + "  x " + totalQuantity(queue)));
        System.out.println("--------------------------------------");
    }

    private int totalQuantity(ArrayDeque<Order> queue) {
        int total = 0;
        for (Order o : queue) {
            total += o.getRemainingQuantity();
        }
        return total;
    }
}