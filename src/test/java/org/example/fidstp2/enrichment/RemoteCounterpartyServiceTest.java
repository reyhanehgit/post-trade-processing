package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.service.ReliabilityMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteCounterpartyServiceTest {

    @Test
    void preloadsConfiguredIdsAndServesSubsequentReadsFromCache() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReliabilityMetrics reliabilityMetrics = mock(ReliabilityMetrics.class);
        when(restTemplate.getForObject("http://localhost:8888/api/reference/counterparties/CP-1", Map.class))
                .thenReturn(Map.of("id", "CP-1", "name", "Counterparty One", "active", true));

        RemoteCounterpartyService service = new RemoteCounterpartyService(
                restTemplate,
                "http://localhost:8888",
                100,
                30,
                false,
                "CP-1",
                reliabilityMetrics
        );

        Counterparty first = service.getCounterparty("CP-1");
        Counterparty second = service.getCounterparty("CP-1");

        assertEquals("CP-1", first.id());
        assertEquals("CP-1", second.id());
        verify(restTemplate, times(1))
                .getForObject("http://localhost:8888/api/reference/counterparties/CP-1", Map.class);
        verify(reliabilityMetrics, times(1)).recordCounterpartyCachePreload(org.mockito.ArgumentMatchers.eq(1), any(Duration.class));
    }

    @Test
    void preloadsAllFromListEndpointWhenEnabled() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReliabilityMetrics reliabilityMetrics = mock(ReliabilityMetrics.class);
        when(restTemplate.getForObject("http://localhost:8888/api/reference/counterparties", List.class))
                .thenReturn(List.of(Map.of("id", "CP-1", "name", "Counterparty One", "active", true)));

        RemoteCounterpartyService service = new RemoteCounterpartyService(
                restTemplate,
                "http://localhost:8888",
                100,
                30,
                true,
                "",
                reliabilityMetrics
        );

        Counterparty first = service.getCounterparty("CP-1");
        Counterparty second = service.getCounterparty("CP-1");

        assertEquals("CP-1", first.id());
        assertEquals("CP-1", second.id());
        verify(restTemplate, times(1))
                .getForObject("http://localhost:8888/api/reference/counterparties", List.class);
        verify(restTemplate, times(0))
                .getForObject("http://localhost:8888/api/reference/counterparties/CP-1", Map.class);
        verify(reliabilityMetrics, times(1)).recordCounterpartyCachePreload(org.mockito.ArgumentMatchers.eq(1), any(Duration.class));
    }
}

