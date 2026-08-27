package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.persistence.CounterpartyEntity;
import org.example.fidstp2.repository.CounterpartyRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaCounterpartyServiceTest {

    @Test
    void returnsPreloadedCounterpartyWithoutRepositoryLookupById() {
        CounterpartyRepository repository = mock(CounterpartyRepository.class);
        CounterpartyEntity entity = counterpartyEntity();

        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findById("CP-1")).thenReturn(Optional.of(entity));

        JpaCounterpartyService service = new JpaCounterpartyService(repository);

        Counterparty counterparty = service.getCounterparty("CP-1");

        assertEquals("CP-1", counterparty.id());
        assertEquals("Counterparty One", counterparty.name());
        verify(repository, never()).findById("CP-1");
    }

    private CounterpartyEntity counterpartyEntity() {
        CounterpartyEntity entity = new CounterpartyEntity();
        entity.setCounterpartyId("CP-1");
        entity.setName("Counterparty One");
        entity.setActive(true);
        return entity;
    }
}

