package org.example.currencypairservice.repository;

import org.example.currencypairservice.entity.CurrencyPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyPairRepository extends JpaRepository<CurrencyPairEntity, String> {
}

