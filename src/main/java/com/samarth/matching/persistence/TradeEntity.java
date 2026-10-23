package com.samarth.matching.persistence;

import com.samarth.matching.model.Trade;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "trades")
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    private long buyOrderId;
    private long sellOrderId;
    private long price;
    private int quantity;

    @Column(nullable = false)
    private Instant executedAt;

    protected TradeEntity() {
        // required by JPA
    }

    public static TradeEntity from(Trade trade) {
        TradeEntity entity = new TradeEntity();
        entity.symbol = trade.symbol();
        entity.buyOrderId = trade.buyOrderId();
        entity.sellOrderId = trade.sellOrderId();
        entity.price = trade.price();
        entity.quantity = trade.quantity();
        entity.executedAt = Instant.now();
        return entity;
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public long getBuyOrderId() { return buyOrderId; }
    public long getSellOrderId() { return sellOrderId; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public Instant getExecutedAt() { return executedAt; }
}