package com.samarth.matching.model;

public record Trade(String symbol, long buyOrderId, long sellOrderId, long price, int quantity) {

    public static String formatPrice(long paise) {
        return String.format("₹%d.%02d", paise / 100, paise % 100);
    }

    @Override
    public String toString() {
        return String.format("TRADE %s | %d @ %s | buy #%d, sell #%d",
                symbol, quantity, formatPrice(price), buyOrderId, sellOrderId);
    }
}