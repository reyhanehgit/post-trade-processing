package org.example.fidstp2.service;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.enrichment.CounterpartyService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CounterpartyQueryServiceTest {

    @Test
    void delegatesToCounterpartyService() {
        CounterpartyService counterpartyService = mock(CounterpartyService.class);
        when(counterpartyService.getCounterparty("CP-1")).thenReturn(new Counterparty("CP-1", "Bank A", true));

        CounterpartyQueryService queryService = new CounterpartyQueryService(counterpartyService);
        Counterparty result = queryService.getById("CP-1");

        verify(counterpartyService).getCounterparty("CP-1");
        assertEquals("CP-1", result.id());
        assertEquals("Bank A", result.name());
        assertEquals(true, result.active());
    }
}

