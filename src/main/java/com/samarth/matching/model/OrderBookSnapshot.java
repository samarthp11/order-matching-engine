package com.samarth.matching.model;

import java.util.List;

public record OrderBookSnapshot(String symbol, List<PriceLevel> bids, List<PriceLevel> asks) {
}