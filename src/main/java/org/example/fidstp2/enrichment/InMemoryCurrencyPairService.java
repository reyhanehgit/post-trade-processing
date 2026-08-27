package org.example.fidstp2.enrichment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;

import java.util.Map;

public class InMemoryCurrencyPairService implements CurrencyPairService {
    private final Cache<String, CurrencyPair> currencyPairCache;

    public InMemoryCurrencyPairService(Map<String, CurrencyPair> currencyPairs) {
        Map<String, CurrencyPair> immutableCurrencyPairs = Map.copyOf(currencyPairs);
        this.currencyPairCache = CacheBuilder.newBuilder()
                .maximumSize(Math.max(1, immutableCurrencyPairs.size()))
                .build();
        this.currencyPairCache.putAll(immutableCurrencyPairs);
    }

    @Override
    public CurrencyPair getCurrencyPair(String symbol) {
        CurrencyPair currencyPair = currencyPairCache.getIfPresent(symbol);
        if (currencyPair == null) {
            throw new ReferenceDataNotFoundException("currency pair not found: " + symbol);
        }
        return currencyPair;
    }
}

