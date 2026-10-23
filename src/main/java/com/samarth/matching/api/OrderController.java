package com.samarth.matching.api;

import com.samarth.matching.api.dto.PlaceOrderRequest;
import com.samarth.matching.model.OrderBookSnapshot;
import com.samarth.matching.model.OrderResult;
import com.samarth.matching.persistence.TradeEntity;
import com.samarth.matching.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResult> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResult result = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/orders/{symbol}/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String symbol, @PathVariable long orderId) {
        boolean cancelled = orderService.cancelOrder(symbol, orderId);
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/orderbook/{symbol}")
    public OrderBookSnapshot getOrderBook(@PathVariable String symbol) {
        return orderService.getOrderBook(symbol);
    }

    @GetMapping("/trades")
    public List<TradeEntity> getTrades(@RequestParam String symbol,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return orderService.getRecentTrades(symbol, page, size);
    }
}