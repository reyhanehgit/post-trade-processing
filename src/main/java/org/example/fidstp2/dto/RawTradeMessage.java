package org.example.fidstp2.dto;

import java.time.Instant;
import java.util.Objects;

public record RawTradeMessage(
        String sourceSystem,
        String payload,
        String correlationId,
        Instant receivedAt
) {
    public RawTradeMessage {
        Objects.requireNonNull(sourceSystem, "sourceSystem is required");
        Objects.requireNonNull(payload, "payload is required");
        Objects.requireNonNull(correlationId, "correlationId is required");
        Objects.requireNonNull(receivedAt, "receivedAt is required");
    }
}

