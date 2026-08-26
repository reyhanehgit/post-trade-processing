package org.example.fidstp2.repository;

import org.example.fidstp2.persistence.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    Optional<TradeEntity> findByTradeId(String tradeId);
}

