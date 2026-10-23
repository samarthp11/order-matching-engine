package com.samarth.matching.model;

import java.util.List;

public record OrderResult(long orderId, int quantity, int filledQuantity,
                          int remainingQuantity, boolean restingInBook, List<Trade> trades) {
}