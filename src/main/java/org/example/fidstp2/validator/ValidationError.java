package org.example.fidstp2.validator;

import java.time.Instant;
import java.util.Objects;

public record ValidationError(
        String tradeId,
        String stage,
        String errorCode,
        String message,
        Instant timestamp
) {
    public ValidationError {
        Objects.requireNonNull(stage, "stage is required");
        Objects.requireNonNull(errorCode, "errorCode is required");
        Objects.requireNonNull(message, "message is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
    }
}

