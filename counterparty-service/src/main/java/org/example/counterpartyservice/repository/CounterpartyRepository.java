package org.example.counterpartyservice.repository;

import org.example.counterpartyservice.entity.CounterpartyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterpartyRepository extends JpaRepository<CounterpartyEntity, String> {
}

