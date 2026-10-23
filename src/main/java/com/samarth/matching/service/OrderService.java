package com.samarth.matching.service;

import com.samarth.matching.api.dto.PlaceOrderRequest;
import com.samarth.matching.book.OrderBook;
import com.samarth.matching.engine.MatchingEngine;
import com.samarth.matching.exception.InvalidOrderException;
import com.samarth.matching.model.OrderBookSnapshot;
import com.samarth.matching.model.OrderResult;
import com.samarth.matching.model.OrderType;
import com.samarth.matching.persistence.TradeEntity;
import com.samarth.matching.persistence.TradeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private static final int MAX_PAGE_SIZE = 100;

    private final MatchingEngine engine;
    private final TradeRepository tradeRepository;

    public OrderService(MatchingEngine engine, TradeRepository tradeRepository) {
        this.engine = engine;
        this.tradeRepository = tradeRepository;
    }

    public OrderResult placeOrder(PlaceOrderRequest request) {
        String symbol = normalise(request.symbol());
        OrderResult result;

        if (request.type() == OrderType.LIMIT) {
            if (request.price() == null) {
                throw new InvalidOrderException("price is required for LIMIT orders");
            }
            result = engine.executeLimitOrder(symbol, request.side(), request.price(), request.quantity());
        } else {
            result = engine.executeMarketOrder(symbol, request.side(), request.quantity());
        }

        if (!result.trades().isEmpty()) {
            tradeRepository.saveAll(result.trades().stream().map(TradeEntity::from).toList());
        }
        return result;
    }

    public boolean cancelOrder(String symbol, long orderId) {
        return engine.cancelOrder(normalise(symbol), orderId);
    }

    public OrderBookSnapshot getOrderBook(String symbol) {
        String normalised = normalise(symbol);
        OrderBook book = engine.getBook(normalised);
        if (book == null) {
            return new OrderBookSnapshot(normalised, List.of(), List.of());
        }
        return book.snapshot();
    }

    public List<TradeEntity> getRecentTrades(String symbol, int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return tradeRepository.findBySymbolOrderByIdDesc(normalise(symbol), PageRequest.of(page, safeSize));
    }

    private String normalise(String symbol) {
        return symbol.trim().toUpperCase();
    }
}