    package org.example.fidstp2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.persistence.OutboxEventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxRepublisherServiceTest {

    @Test
    void republishesDueOutboxEvent() throws Exception {
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        OutboxEventEntity dueEvent = createOutboxEvent(42L, "T-904", "{\"tradeId\":\"T-904\"}");

        when(persistenceService.findDueOutboxEventsForRetry(100)).thenReturn(List.of(dueEvent));
        when(kafkaTemplate.send(eq("fx.option.trade.processed"), eq("T-904"), eq("{\"tradeId\":\"T-904\"}")))
                .thenReturn(CompletableFuture.completedFuture(null));

        OutboxRepublisherService service = new OutboxRepublisherService(
                persistenceService,
                new ObjectMapper(),
                kafkaTemplate,
                "fx.option.trade.processed",
                "fx.option.trade.dlq",
                5,
                Duration.ofMillis(500),
                100,
                new ReliabilityMetrics(new SimpleMeterRegistry())
        );

        service.republishDueEvents();

        verify(persistenceService).markOutboxEventPublished(42L);
        verify(persistenceService).appendProcessingHistory("T-904", ProcessingStatus.PUBLISHED, "PUBLISH_RETRY", "retry publish succeeded");
    }

    @Test
    void sendsToDlqAfterRetryExhaustion() throws Exception {
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        OutboxEventEntity dueEvent = createOutboxEvent(43L, "T-905", "{\"tradeId\":\"T-905\"}");

        when(persistenceService.findDueOutboxEventsForRetry(100)).thenReturn(List.of(dueEvent));
        when(kafkaTemplate.send(eq("fx.option.trade.processed"), eq("T-905"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));
        when(persistenceService.markOutboxEventForRetry(eq(43L), any(), eq(3), eq(Duration.ofMillis(250))))
                .thenReturn(true);
        when(kafkaTemplate.send(eq("fx.option.trade.dlq"), eq("T-905"), contains("broker down")))
                .thenReturn(CompletableFuture.completedFuture(null));

        OutboxRepublisherService service = new OutboxRepublisherService(
                persistenceService,
                new ObjectMapper(),
                kafkaTemplate,
                "fx.option.trade.processed",
                "fx.option.trade.dlq",
                3,
                Duration.ofMillis(250),
                100,
                new ReliabilityMetrics(new SimpleMeterRegistry())
        );

        service.republishDueEvents();

        verify(persistenceService).appendProcessingHistory("T-905", ProcessingStatus.DLQ, "PUBLISH_RETRY", "outbox moved to DLQ after max retries");
        verify(kafkaTemplate).send(eq("fx.option.trade.dlq"), eq("T-905"), contains("broker down"));
    }

    private static OutboxEventEntity createOutboxEvent(Long id, String aggregateId, String payload) throws Exception {
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        setId(outboxEvent, id);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType("ProcessedTradeEvent");
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus("PENDING_RETRY");
        outboxEvent.setCreatedAt(Instant.parse("2026-08-26T10:00:00Z"));
        return outboxEvent;
    }

    private static void setId(OutboxEventEntity outboxEvent, Long id) throws Exception {
        Field field = OutboxEventEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(outboxEvent, id);
    }
}

