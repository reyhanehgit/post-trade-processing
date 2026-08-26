package org.example.fidstp2.dto;

import java.time.Instant;
import java.util.Objects;

public record ProcessedTradeEvent(
        String eventVersion,
        String tradeId,
        String status,
        Instant publishedAt
) {
    public ProcessedTradeEvent {
        Objects.requireNonNull(eventVersion, "eventVersion is required");
        Objects.requireNonNull(tradeId, "tradeId is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(publishedAt, "publishedAt is required");
    }
}

