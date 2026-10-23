package com.samarth.matching.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    List<TradeEntity> findBySymbolOrderByIdDesc(String symbol, Pageable pageable);
}