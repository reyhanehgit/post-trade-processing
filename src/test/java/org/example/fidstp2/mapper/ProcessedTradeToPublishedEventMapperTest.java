package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessedTradeToPublishedEventMapperTest {

    @Test
    void mapsProcessedTradeToPublishedEvent() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-26T12:00:00Z"), ZoneOffset.UTC);
        ProcessedTradeToPublishedEventMapper mapper = new ProcessedTradeToPublishedEventMapper(clock);
        ProcessedTrade trade = new ProcessedTrade(
                "T-900",
                ProcessingStatus.PROCESSED,
                Instant.parse("2026-08-26T11:59:00Z"),
                List.of("ok")
        );

        ProcessedTradeEvent event = mapper.map(trade);

        assertEquals("1.0", event.eventVersion());
        assertEquals("T-900", event.tradeId());
        assertEquals("PROCESSED", event.status());
        assertEquals(Instant.parse("2026-08-26T12:00:00Z"), event.publishedAt());
    }
}

