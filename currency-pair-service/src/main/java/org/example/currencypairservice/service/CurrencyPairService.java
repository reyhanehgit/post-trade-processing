package org.example.currencypairservice.service;

import org.example.currencypairservice.dto.CurrencyPairResponse;
import org.example.currencypairservice.entity.CurrencyPairEntity;
import org.example.currencypairservice.repository.CurrencyPairRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyPairService {
    private final CurrencyPairRepository repository;

    public CurrencyPairService(CurrencyPairRepository repository) {
        this.repository = repository;
    }

    public CurrencyPairResponse getById(String id) {
        CurrencyPairEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency pair not found: " + id));
        return new CurrencyPairResponse(entity.getId(), entity.getBaseCurrency(),
                entity.getQuoteCurrency(), entity.getDescription());
    }

    public List<CurrencyPairResponse> getAll() {
        return repository.findAll().stream()
                .map(entity -> new CurrencyPairResponse(entity.getId(), entity.getBaseCurrency(),
                        entity.getQuoteCurrency(), entity.getDescription()))
                .toList();
    }
}

