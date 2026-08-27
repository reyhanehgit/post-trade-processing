package org.example.fidstp2.config;

import org.example.fidstp2.enrichment.CurrencyPairService;
import org.example.fidstp2.enrichment.RemoteCurrencyPairService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fidstp2_currency_pair_selection;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "app.kafka.listener.enabled=false",
        "enrichment.currency-pair.remote.enabled=true",
        "enrichment.currency-pair.remote.base-url=http://localhost:8889"
})
class CurrencyPairServiceBeanSelectionTest {

    @Autowired
    private CurrencyPairService currencyPairService;

    @Test
    void usesRemoteCurrencyPairServiceWhenRemoteFlagEnabled() {
        assertInstanceOf(RemoteCurrencyPairService.class, currencyPairService);
    }
}

