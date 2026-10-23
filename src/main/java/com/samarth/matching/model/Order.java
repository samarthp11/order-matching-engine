package com.samarth.matching.model;

public class Order {
    private final long id;
    private final String symbol;
    private final Side side;
    private final OrderType type;
    private final long price;          // in paise, so ₹502.50 is stored as 50250
    private final int quantity;
    private int remainingQuantity;

    public Order(long id, String symbol, Side side, OrderType type, long price, int quantity) {
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
    }

    public void fill(int qty) {
        remainingQuantity -= qty;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }

    public long getId() { return id; }
    public String getSymbol() { return symbol; }
    public Side getSide() { return side; }
    public OrderType getType() { return type; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
}