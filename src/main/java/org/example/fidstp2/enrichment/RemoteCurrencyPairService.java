package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "enrichment.currency-pair.remote.enabled", havingValue = "true")
public class RemoteCurrencyPairService implements CurrencyPairService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RemoteCurrencyPairService(RestTemplate restTemplate,
                                     @Value("${enrichment.currency-pair.remote.base-url:http://localhost:8889}") String baseUrl) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate is required");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl is required");
    }

    @Override
    public CurrencyPair getCurrencyPair(String id) {
        try {
            String url = baseUrl + "/api/reference/currency-pairs/" + id;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new ReferenceDataNotFoundException("Currency pair not found: " + id);
            }
            return new CurrencyPair(
                    (String) response.get("id"),
                    (String) response.get("baseCurrency"),
                    (String) response.get("quoteCurrency")
            );
        } catch (RestClientException e) {
            throw new ReferenceDataNotFoundException("Failed to fetch currency pair " + id + ": " + e.getMessage());
        }
    }
}

