package org.example.counterpartyservice.service;

import org.example.counterpartyservice.dto.CounterpartyResponse;
import org.example.counterpartyservice.entity.CounterpartyEntity;
import org.example.counterpartyservice.repository.CounterpartyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounterpartyService {
    private final CounterpartyRepository repository;

    public CounterpartyService(CounterpartyRepository repository) {
        this.repository = repository;
    }

    public CounterpartyResponse getById(String id) {
        CounterpartyEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counterparty not found: " + id));
        return new CounterpartyResponse(entity.getId(), entity.getName(), entity.isActive());
    }

    public List<CounterpartyResponse> getAll() {
        return repository.findAll().stream()
                .map(entity -> new CounterpartyResponse(entity.getId(), entity.getName(), entity.isActive()))
                .toList();
    }
}

