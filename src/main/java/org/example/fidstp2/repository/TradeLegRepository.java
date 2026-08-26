package org.example.fidstp2.repository;

import org.example.fidstp2.persistence.TradeLegEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeLegRepository extends JpaRepository<TradeLegEntity, Long> {
    List<TradeLegEntity> findByTrade_TradeId(String tradeId);
}

