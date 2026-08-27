package org.example.fidstp2.service;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.exception.PublishingException;
import org.example.fidstp2.mapper.ProcessedTradeToPublishedEventMapper;
import org.example.fidstp2.persistence.OutboxEventStatus;
import org.example.fidstp2.publisher.PublishedEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Objects;

public class TradePublicationService {
    private final ProcessedTradeToPublishedEventMapper mapper;
    private final PublishedEventPublisher<ProcessedTradeEvent> publisher;
    private final TradePersistenceService persistenceService;
    private final ObjectMapper objectMapper;
    private final int maxOutboxPublishAttempts;
    private final Duration outboxRetryBackoff;

    public TradePublicationService(
            ProcessedTradeToPublishedEventMapper mapper,
            PublishedEventPublisher<ProcessedTradeEvent> publisher,
            TradePersistenceService persistenceService,
            ObjectMapper objectMapper,
            int maxOutboxPublishAttempts,
            Duration outboxRetryBackoff
    ) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.publisher = Objects.requireNonNull(publisher, "publisher is required");
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
        if (maxOutboxPublishAttempts < 1) {
            throw new IllegalArgumentException("maxOutboxPublishAttempts must be >= 1");
        }
        this.maxOutboxPublishAttempts = maxOutboxPublishAttempts;
        this.outboxRetryBackoff = Objects.requireNonNull(outboxRetryBackoff, "outboxRetryBackoff is required");
    }

    public ProcessedTradeEvent publish(ProcessedTrade processedTrade) {
        ProcessedTradeEvent event = mapper.map(processedTrade);
        Long outboxId = persistenceService.createOutboxEvent(
                processedTrade.getTradeId(),
                ProcessedTradeEvent.class.getSimpleName(),
                toJson(event),
                OutboxEventStatus.NEW.value()
        );

        try {
            publisher.publish(processedTrade.getTradeId(), event);
            persistenceService.markOutboxEventPublished(outboxId);
            persistenceService.appendProcessingHistory(
                    processedTrade.getTradeId(),
                    ProcessingStatus.PUBLISHED,
                    "PUBLISH",
                    "published to outbound topic"
            );
            return event;
        } catch (RuntimeException ex) {
            boolean movedToDlq = persistenceService.markOutboxEventForRetry(
                    outboxId,
                    ex.getMessage(),
                    maxOutboxPublishAttempts,
                    outboxRetryBackoff
            );
            persistenceService.appendProcessingHistory(
                    processedTrade.getTradeId(),
                    movedToDlq ? ProcessingStatus.DLQ : ProcessingStatus.PUBLISH_FAILED,
                    "PUBLISH",
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()
            );
            throw ex;
        }
    }

    private String toJson(ProcessedTradeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new PublishingException("failed to serialize outbox event payload", ex);
        }
    }
}

