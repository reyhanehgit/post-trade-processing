package org.example.currencypairservice.controller;

import org.example.currencypairservice.dto.CurrencyPairResponse;
import org.example.currencypairservice.service.CurrencyPairService;
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
@RequestMapping("/api/reference/currency-pairs")
public class CurrencyPairController {
    private final CurrencyPairService service;

    public CurrencyPairController(CurrencyPairService service) {
        this.service = service;
    }

    // Support path: GET /api/reference/currency-pairs/EUR/USD
    @GetMapping("/{base}/{quote}")
    public CurrencyPairResponse getCurrencyPair(@PathVariable String base,
                                                @PathVariable String quote) {
        String id = base + "/" + quote;
        return service.getById(id);
    }

    @GetMapping
    public List<CurrencyPairResponse> getAllCurrencyPairs() {
        return service.getAll();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}



