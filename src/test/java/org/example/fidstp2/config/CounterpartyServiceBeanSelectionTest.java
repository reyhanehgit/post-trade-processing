package org.example.fidstp2.config;

import org.example.fidstp2.enrichment.CounterpartyService;
import org.example.fidstp2.enrichment.RemoteCounterpartyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fidstp2_counterparty_selection;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "app.kafka.listener.enabled=false",
        "enrichment.counterparty.remote.enabled=true",
        "enrichment.counterparty.remote.base-url=http://localhost:8888"
})
class CounterpartyServiceBeanSelectionTest {

    @Autowired
    private CounterpartyService counterpartyService;

    @Test
    void usesRemoteCounterpartyServiceWhenRemoteFlagEnabled() {
        assertInstanceOf(RemoteCounterpartyService.class, counterpartyService);
    }
}

