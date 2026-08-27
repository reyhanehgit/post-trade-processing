package org.example.fidstp2.controller;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.dto.CounterpartyResponse;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.example.fidstp2.service.CounterpartyQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/reference/counterparties")
public class CounterpartyController {
    private final CounterpartyQueryService queryService;

    public CounterpartyController(CounterpartyQueryService queryService) {
        this.queryService = Objects.requireNonNull(queryService, "queryService is required");
    }

    @GetMapping("/{id}")
    public CounterpartyResponse getCounterparty(@PathVariable String id) {
        Counterparty counterparty = queryService.getById(id);
        return new CounterpartyResponse(counterparty.id(), counterparty.name(), counterparty.active());
    }

    @ExceptionHandler(ReferenceDataNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ReferenceDataNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}

