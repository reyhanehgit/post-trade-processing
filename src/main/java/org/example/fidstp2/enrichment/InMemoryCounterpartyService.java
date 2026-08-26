package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;

import java.util.Map;

public class InMemoryCounterpartyService implements CounterpartyService {
    private final Map<String, Counterparty> counterparties;

    public InMemoryCounterpartyService(Map<String, Counterparty> counterparties) {
        this.counterparties = Map.copyOf(counterparties);
    }

    @Override
    public Counterparty getCounterparty(String id) {
        Counterparty counterparty = counterparties.get(id);
        if (counterparty == null) {
            throw new ReferenceDataNotFoundException("counterparty not found: " + id);
        }
        return counterparty;
    }
}

