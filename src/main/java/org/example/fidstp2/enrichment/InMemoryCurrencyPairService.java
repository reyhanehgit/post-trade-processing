package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;

import java.util.Map;

public class InMemoryCurrencyPairService implements CurrencyPairService {
    private final Map<String, CurrencyPair> currencyPairs;

    public InMemoryCurrencyPairService(Map<String, CurrencyPair> currencyPairs) {
        this.currencyPairs = Map.copyOf(currencyPairs);
    }

    @Override
    public CurrencyPair getCurrencyPair(String symbol) {
        CurrencyPair currencyPair = currencyPairs.get(symbol);
        if (currencyPair == null) {
            throw new ReferenceDataNotFoundException("currency pair not found: " + symbol);
        }
        return currencyPair;
    }
}

