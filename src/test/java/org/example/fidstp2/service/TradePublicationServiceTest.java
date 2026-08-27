package org.example.fidstp2.service;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.mapper.ProcessedTradeToPublishedEventMapper;
import org.example.fidstp2.publisher.PublishedEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradePublicationServiceTest {

    @Test
    void mapsAndPublishesTradeUsingTradeIdAsKey() {
        @SuppressWarnings("unchecked")
        PublishedEventPublisher<ProcessedTradeEvent> publisher = mock(PublishedEventPublisher.class);
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        when(persistenceService.createOutboxEvent(eq("T-902"), eq("ProcessedTradeEvent"), any(), eq("NEW"))).thenReturn(10L);
        Clock clock = Clock.fixed(Instant.parse("2026-08-26T12:30:00Z"), ZoneOffset.UTC);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TradePublicationService service = new TradePublicationService(
                new ProcessedTradeToPublishedEventMapper(clock),
                publisher,
                persistenceService,
                objectMapper
        );
        ProcessedTrade trade = new ProcessedTrade(
                "T-902",
                ProcessingStatus.PROCESSED,
                Instant.parse("2026-08-26T12:00:00Z"),
                List.of("ok")
        );

        ProcessedTradeEvent event = service.publish(trade);

        verify(publisher).publish("T-902", event);
        verify(persistenceService).markOutboxEventPublished(10L);
        verify(persistenceService).appendProcessingHistory("T-902", ProcessingStatus.PUBLISHED, "PUBLISH", "published to outbound topic");
        assertEquals("T-902", event.tradeId());
    }
}

