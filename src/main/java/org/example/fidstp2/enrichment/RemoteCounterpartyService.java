package org.example.fidstp2.enrichment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.example.fidstp2.domain.Counterparty;
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
@ConditionalOnProperty(name = "enrichment.counterparty.remote.enabled", havingValue = "true")
public class RemoteCounterpartyService implements CounterpartyService {
    private static final Logger log = LoggerFactory.getLogger(RemoteCounterpartyService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final LoadingCache<String, Counterparty> cache;
    private final ReliabilityMetrics reliabilityMetrics;

    public RemoteCounterpartyService(RestTemplate restTemplate,
                                     @Value("${enrichment.counterparty.remote.base-url:http://localhost:8888}") String baseUrl,
                                     @Value("${enrichment.counterparty.cache.max-size:10000}") long maxSize,
                                     @Value("${enrichment.counterparty.cache.ttl-minutes:30}") long ttlMinutes,
                                     @Value("${enrichment.counterparty.cache.preload-all:true}") boolean preloadAll,
                                     @Value("${enrichment.counterparty.cache.preload-ids:}") String preloadIdsCsv,
                                     ReliabilityMetrics reliabilityMetrics) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate is required");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl is required");
        this.reliabilityMetrics = Objects.requireNonNull(reliabilityMetrics, "reliabilityMetrics is required");
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(Math.max(1, maxSize))
                .expireAfterWrite(Duration.ofMinutes(Math.max(1, ttlMinutes)))
                .build(CacheLoader.from(this::fetchCounterparty));
        long startNanos = System.nanoTime();
        int loadedRecords = 0;
        if (preloadAll) {
            loadedRecords += preloadAllCounterparties();
        }
        loadedRecords += preloadConfiguredCounterparties(preloadIdsCsv);
        reliabilityMetrics.recordCounterpartyCachePreload(loadedRecords, Duration.ofNanos(System.nanoTime() - startNanos));
    }

    @Override
    public Counterparty getCounterparty(String id) {
        return cache.getUnchecked(id);
    }

    private Counterparty fetchCounterparty(String id) {
        try {
            String url = baseUrl + "/api/reference/counterparties/" + id;
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new ReferenceDataNotFoundException("Counterparty not found: " + id);
            }
            return new Counterparty(
                    String.valueOf(response.get("id")),
                    String.valueOf(response.get("name")),
                    Boolean.TRUE.equals(response.get("active"))
            );
        } catch (RestClientException e) {
            throw new ReferenceDataNotFoundException("Failed to fetch counterparty " + id + ": " + e.getMessage());
        }
    }

    private int preloadConfiguredCounterparties(String preloadIdsCsv) {
        int loadedCount = 0;
        for (String id : parseCsv(preloadIdsCsv)) {
            cache.put(id, fetchCounterparty(id));
            loadedCount++;
        }
        return loadedCount;
    }

    private int preloadAllCounterparties() {
        String url = baseUrl + "/api/reference/counterparties";
        try {
            List<?> response = restTemplate.getForObject(url, List.class);
            if (response == null) {
                return 0;
            }
            List<Counterparty> counterparties = response.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::toCounterparty)
                    .toList();
            counterparties.forEach(counterparty -> cache.put(counterparty.id(), counterparty));
            return counterparties.size();
        } catch (RestClientException ex) {
            log.warn("Unable to preload all counterparties from {}: {}", url, ex.getMessage());
            return 0;
        }
    }

    private Counterparty toCounterparty(Map<?, ?> response) {
        return new Counterparty(
                String.valueOf(response.get("id")),
                String.valueOf(response.get("name")),
                Boolean.TRUE.equals(response.get("active"))
        );
    }

    private List<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}

