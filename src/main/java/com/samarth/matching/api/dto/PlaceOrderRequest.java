package com.samarth.matching.api.dto;

import com.samarth.matching.model.OrderType;
import com.samarth.matching.model.Side;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlaceOrderRequest(
        @NotBlank(message = "symbol is required")
        String symbol,

        @NotNull(message = "side is required (BUY or SELL)")
        Side side,

        @NotNull(message = "type is required (LIMIT or MARKET)")
        OrderType type,

        Long price,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        Integer quantity
) {
}