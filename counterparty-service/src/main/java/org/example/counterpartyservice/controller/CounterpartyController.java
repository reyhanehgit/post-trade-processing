package org.example.counterpartyservice.controller;

import org.example.counterpartyservice.dto.CounterpartyResponse;
import org.example.counterpartyservice.service.CounterpartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reference/counterparties")
public class CounterpartyController {
    private final CounterpartyService service;

    public CounterpartyController(CounterpartyService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public CounterpartyResponse getCounterparty(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CounterpartyResponse> getAllCounterparties() {
        return service.getAll();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}

