package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.example.fidstp2.repository.CounterpartyRepository;

import java.util.Objects;

public class JpaCounterpartyService implements CounterpartyService {
    private final CounterpartyRepository repository;

    public JpaCounterpartyService(CounterpartyRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    @Override
    public Counterparty getCounterparty(String id) {
        return repository.findById(id)
                .map(entity -> new Counterparty(entity.getCounterpartyId(), entity.getName(), entity.isActive()))
                .orElseThrow(() -> new ReferenceDataNotFoundException("counterparty not found: " + id));
    }
}

