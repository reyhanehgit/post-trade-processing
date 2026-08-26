package org.example.fidstp2.domain;

public enum ProcessingStatus {
    RECEIVED,
    PARSED,
    VALIDATED,
    ENRICHING,
    ENRICHED,
    PROCESSED,
    PUBLISHED,
    VALIDATION_FAILED,
    ENRICHMENT_FAILED,
    PROCESSING_FAILED,
    PUBLISH_FAILED,
    DLQ
}

