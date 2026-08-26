package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.dto.ProcessedTradeEvent;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ProcessedTradeToPublishedEventMapper {
    private static final String EVENT_VERSION = "1.0";

    private final Clock clock;

    public ProcessedTradeToPublishedEventMapper() {
        this(Clock.systemUTC());
    }

    public ProcessedTradeToPublishedEventMapper(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public ProcessedTradeEvent map(ProcessedTrade trade) {
        Objects.requireNonNull(trade, "trade is required");
        Instant publishedAt = Instant.now(clock);
        return new ProcessedTradeEvent(
                EVENT_VERSION,
                trade.getTradeId(),
                trade.getStatus().name(),
                publishedAt
        );
    }
}

