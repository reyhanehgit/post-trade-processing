package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "enrichment.counterparty.remote.enabled", havingValue = "true")
public class RemoteCounterpartyService implements CounterpartyService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RemoteCounterpartyService(RestTemplate restTemplate,
                                     @Value("${enrichment.counterparty.remote.base-url:http://localhost:8888}") String baseUrl) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate is required");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl is required");
    }

    @Override
    public Counterparty getCounterparty(String id) {
        try {
            String url = baseUrl + "/api/reference/counterparties/" + id;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new ReferenceDataNotFoundException("Counterparty not found: " + id);
            }
            return new Counterparty(
                    (String) response.get("id"),
                    (String) response.get("name"),
                    (Boolean) response.get("active")
            );
        } catch (RestClientException e) {
            throw new ReferenceDataNotFoundException("Failed to fetch counterparty " + id + ": " + e.getMessage());
        }
    }
}

