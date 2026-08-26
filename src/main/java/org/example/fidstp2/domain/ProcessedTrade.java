package org.example.fidstp2.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProcessedTrade {
    private final String tradeId;
    private final ProcessingStatus status;
    private final Instant processedAt;
    private final List<String> processingNotes;

    public ProcessedTrade(String tradeId, ProcessingStatus status, Instant processedAt, List<String> processingNotes) {
        if (tradeId == null || tradeId.isBlank()) {
            throw new IllegalArgumentException("tradeId is required");
        }
        this.tradeId = tradeId;
        this.status = Objects.requireNonNull(status, "status is required");
        this.processedAt = Objects.requireNonNull(processedAt, "processedAt is required");
        this.processingNotes = processingNotes == null ? List.of() : List.copyOf(new ArrayList<>(processingNotes));
    }

    public String getTradeId() {
        return tradeId;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public List<String> getProcessingNotes() {
        return processingNotes;
    }
}

