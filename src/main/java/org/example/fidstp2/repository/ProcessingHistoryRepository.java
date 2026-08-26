package org.example.fidstp2.repository;

import org.example.fidstp2.persistence.ProcessingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessingHistoryRepository extends JpaRepository<ProcessingHistoryEntity, Long> {
    List<ProcessingHistoryEntity> findByTradeIdOrderByCreatedAtAsc(String tradeId);
}

