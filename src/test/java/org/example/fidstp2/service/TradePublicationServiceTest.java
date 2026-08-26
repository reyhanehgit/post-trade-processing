package org.example.fidstp2.service;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.mapper.ProcessedTradeToPublishedEventMapper;
import org.example.fidstp2.publisher.PublishedEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TradePublicationServiceTest {

    @Test
    void mapsAndPublishesTradeUsingTradeIdAsKey() {
        @SuppressWarnings("unchecked")
        PublishedEventPublisher<ProcessedTradeEvent> publisher = mock(PublishedEventPublisher.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-26T12:30:00Z"), ZoneOffset.UTC);
        TradePublicationService service = new TradePublicationService(
                new ProcessedTradeToPublishedEventMapper(clock),
                publisher
        );
        ProcessedTrade trade = new ProcessedTrade(
                "T-902",
                ProcessingStatus.PROCESSED,
                Instant.parse("2026-08-26T12:00:00Z"),
                List.of("ok")
        );

        ProcessedTradeEvent event = service.publish(trade);

        verify(publisher).publish("T-902", event);
        assertEquals("T-902", event.tradeId());
    }
}

