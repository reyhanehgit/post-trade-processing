package org.example.fidstp2.service;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.enrichment.CounterpartyService;

import java.util.Objects;

public class CounterpartyQueryService {
    private final CounterpartyService counterpartyService;

    public CounterpartyQueryService(CounterpartyService counterpartyService) {
        this.counterpartyService = Objects.requireNonNull(counterpartyService, "counterpartyService is required");
    }

    public Counterparty getById(String counterpartyId) {
        return counterpartyService.getCounterparty(counterpartyId);
    }
}

