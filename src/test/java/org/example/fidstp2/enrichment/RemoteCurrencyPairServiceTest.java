package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.CurrencyPair;
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

class RemoteCurrencyPairServiceTest {

    @Test
    void preloadsConfiguredIdsAndServesSubsequentReadsFromCache() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReliabilityMetrics reliabilityMetrics = mock(ReliabilityMetrics.class);
        when(restTemplate.getForObject("http://localhost:8889/api/reference/currency-pairs/EUR/USD", Map.class))
                .thenReturn(Map.of("id", "EUR/USD", "baseCurrency", "EUR", "quoteCurrency", "USD"));

        RemoteCurrencyPairService service = new RemoteCurrencyPairService(
                restTemplate,
                "http://localhost:8889",
                100,
                30,
                false,
                "EUR/USD",
                reliabilityMetrics
        );

        CurrencyPair first = service.getCurrencyPair("EUR/USD");
        CurrencyPair second = service.getCurrencyPair("EUR/USD");

        assertEquals("EUR/USD", first.symbol());
        assertEquals("EUR/USD", second.symbol());
        verify(restTemplate, times(1))
                .getForObject("http://localhost:8889/api/reference/currency-pairs/EUR/USD", Map.class);
        verify(reliabilityMetrics, times(1)).recordCurrencyPairCachePreload(org.mockito.ArgumentMatchers.eq(1), any(Duration.class));
    }

    @Test
    void preloadsAllFromListEndpointWhenEnabled() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReliabilityMetrics reliabilityMetrics = mock(ReliabilityMetrics.class);
        when(restTemplate.getForObject("http://localhost:8889/api/reference/currency-pairs", List.class))
                .thenReturn(List.of(Map.of("id", "EUR/USD", "baseCurrency", "EUR", "quoteCurrency", "USD")));

        RemoteCurrencyPairService service = new RemoteCurrencyPairService(
                restTemplate,
                "http://localhost:8889",
                100,
                30,
                true,
                "",
                reliabilityMetrics
        );

        CurrencyPair first = service.getCurrencyPair("EUR/USD");
        CurrencyPair second = service.getCurrencyPair("EUR/USD");

        assertEquals("EUR/USD", first.symbol());
        assertEquals("EUR/USD", second.symbol());
        verify(restTemplate, times(1))
                .getForObject("http://localhost:8889/api/reference/currency-pairs", List.class);
        verify(restTemplate, times(0))
                .getForObject("http://localhost:8889/api/reference/currency-pairs/EUR/USD", Map.class);
        verify(reliabilityMetrics, times(1)).recordCurrencyPairCachePreload(org.mockito.ArgumentMatchers.eq(1), any(Duration.class));
    }
}

