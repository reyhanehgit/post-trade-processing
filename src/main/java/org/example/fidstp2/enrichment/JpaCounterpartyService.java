package org.example.fidstp2.enrichment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.example.fidstp2.persistence.CounterpartyEntity;
import org.example.fidstp2.repository.CounterpartyRepository;

import java.util.Objects;

public class JpaCounterpartyService implements CounterpartyService {
    private final CounterpartyRepository repository;
    private final LoadingCache<String, Counterparty> cache;

    public JpaCounterpartyService(CounterpartyRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10_000)
                .build(CacheLoader.from(this::loadCounterpartyById));
        preloadAllCounterparties();
    }

    @Override
    public Counterparty getCounterparty(String id) {
        return cache.getUnchecked(id);
    }

    private Counterparty loadCounterpartyById(String id) {
        return repository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ReferenceDataNotFoundException("counterparty not found: " + id));
    }

    private void preloadAllCounterparties() {
        repository.findAll().stream()
                .map(this::toDomain)
                .forEach(counterparty -> cache.put(counterparty.id(), counterparty));
    }

    private Counterparty toDomain(CounterpartyEntity entity) {
        return new Counterparty(entity.getCounterpartyId(), entity.getName(), entity.isActive());
    }
}

