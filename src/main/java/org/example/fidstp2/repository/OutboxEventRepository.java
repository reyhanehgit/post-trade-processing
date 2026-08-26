package org.example.fidstp2.repository;

import org.example.fidstp2.persistence.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(String status);
}

