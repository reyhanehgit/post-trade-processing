package org.example.fidstp2.enrichment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.example.fidstp2.service.ReliabilityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "enrichment.currency-pair.remote.enabled", havingValue = "true")
public class RemoteCurrencyPairService implements CurrencyPairService {
    private static final Logger log = LoggerFactory.getLogger(RemoteCurrencyPairService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final LoadingCache<String, CurrencyPair> cache;
    private final ReliabilityMetrics reliabilityMetrics;

    public RemoteCurrencyPairService(RestTemplate restTemplate,
                                     @Value("${enrichment.currency-pair.remote.base-url:http://localhost:8889}") String baseUrl,
                                     @Value("${enrichment.currency-pair.cache.max-size:10000}") long maxSize,
                                     @Value("${enrichment.currency-pair.cache.ttl-minutes:30}") long ttlMinutes,
                                     @Value("${enrichment.currency-pair.cache.preload-all:true}") boolean preloadAll,
                                     @Value("${enrichment.currency-pair.cache.preload-ids:}") String preloadIdsCsv,
                                     ReliabilityMetrics reliabilityMetrics) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate is required");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl is required");
        this.reliabilityMetrics = Objects.requireNonNull(reliabilityMetrics, "reliabilityMetrics is required");
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(Math.max(1, maxSize))
                .expireAfterWrite(Duration.ofMinutes(Math.max(1, ttlMinutes)))
                .build(CacheLoader.from(this::fetchCurrencyPair));
        long startNanos = System.nanoTime();
        int loadedRecords = 0;
        if (preloadAll) {
            loadedRecords += preloadAllCurrencyPairs();
        }
        loadedRecords += preloadConfiguredCurrencyPairs(preloadIdsCsv);
        reliabilityMetrics.recordCurrencyPairCachePreload(loadedRecords, Duration.ofNanos(System.nanoTime() - startNanos));
    }

    @Override
    public CurrencyPair getCurrencyPair(String id) {
        return cache.getUnchecked(id);
    }

    private CurrencyPair fetchCurrencyPair(String id) {
        try {
            String url = baseUrl + "/api/reference/currency-pairs/" + id;
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new ReferenceDataNotFoundException("Currency pair not found: " + id);
            }
            return toCurrencyPair(response);
        } catch (RestClientException e) {
            throw new ReferenceDataNotFoundException("Failed to fetch currency pair " + id + ": " + e.getMessage());
        }
    }

    private int preloadConfiguredCurrencyPairs(String preloadIdsCsv) {
        int loadedCount = 0;
        for (String id : parseCsv(preloadIdsCsv)) {
            cache.put(id, fetchCurrencyPair(id));
            loadedCount++;
        }
        return loadedCount;
    }

    private int preloadAllCurrencyPairs() {
        String url = baseUrl + "/api/reference/currency-pairs";
        try {
            List<?> response = restTemplate.getForObject(url, List.class);
            if (response == null) {
                return 0;
            }
            List<CurrencyPair> currencyPairs = response.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::toCurrencyPair)
                    .toList();
            currencyPairs.forEach(currencyPair -> cache.put(currencyPair.symbol(), currencyPair));
            return currencyPairs.size();
        } catch (RestClientException ex) {
            log.warn("Unable to preload all currency pairs from {}: {}", url, ex.getMessage());
            return 0;
        }
    }

    private CurrencyPair toCurrencyPair(Map<?, ?> response) {
        return new CurrencyPair(
                String.valueOf(response.get("id")),
                String.valueOf(response.get("baseCurrency")),
                String.valueOf(response.get("quoteCurrency"))
        );
    }

    private List<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}

