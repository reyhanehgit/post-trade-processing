package org.example.fidstp2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.persistence.OutboxEventEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class OutboxRepublisherService {
    private final TradePersistenceService persistenceService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String outboundTopic;
    private final String dlqTopic;
    private final int maxOutboxPublishAttempts;
    private final Duration outboxRetryBackoff;
    private final int batchSize;
    private final ReliabilityMetrics reliabilityMetrics;

    public OutboxRepublisherService(
            TradePersistenceService persistenceService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate,
            String outboundTopic,
            String dlqTopic,
            int maxOutboxPublishAttempts,
            Duration outboxRetryBackoff,
            int batchSize,
            ReliabilityMetrics reliabilityMetrics
    ) {
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate is required");
        this.outboundTopic = Objects.requireNonNull(outboundTopic, "outboundTopic is required");
        this.dlqTopic = Objects.requireNonNull(dlqTopic, "dlqTopic is required");
        if (maxOutboxPublishAttempts < 1) {
            throw new IllegalArgumentException("maxOutboxPublishAttempts must be >= 1");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be >= 1");
        }
        this.maxOutboxPublishAttempts = maxOutboxPublishAttempts;
        this.outboxRetryBackoff = Objects.requireNonNull(outboxRetryBackoff, "outboxRetryBackoff is required");
        this.batchSize = batchSize;
        this.reliabilityMetrics = Objects.requireNonNull(reliabilityMetrics, "reliabilityMetrics is required");
    }

    @Scheduled(fixedDelayString = "${app.outbox.retry.poll-ms:5000}")
    public void republishDueEvents() {
        List<OutboxEventEntity> dueEvents = persistenceService.findDueOutboxEventsForRetry(batchSize);
        for (OutboxEventEntity outboxEvent : dueEvents) {
            republish(outboxEvent);
        }
    }

    private void republish(OutboxEventEntity outboxEvent) {
        try {
            // Validate payload is still valid JSON before re-publishing.
            objectMapper.readTree(outboxEvent.getPayload());
            kafkaTemplate.send(outboundTopic, outboxEvent.getAggregateId(), outboxEvent.getPayload()).join();
            persistenceService.markOutboxEventPublished(outboxEvent.getId());
            reliabilityMetrics.incrementOutboxRetrySuccess();
            persistenceService.appendProcessingHistory(
                    outboxEvent.getAggregateId(),
                    ProcessingStatus.PUBLISHED,
                    "PUBLISH_RETRY",
                    "retry publish succeeded"
            );
        } catch (Exception ex) {
            boolean movedToDlq = persistenceService.markOutboxEventForRetry(
                    outboxEvent.getId(),
                    ex.getMessage(),
                    maxOutboxPublishAttempts,
                    outboxRetryBackoff
            );
            if (movedToDlq) {
                publishToDlq(outboxEvent, ex);
                persistenceService.appendProcessingHistory(
                        outboxEvent.getAggregateId(),
                        ProcessingStatus.DLQ,
                        "PUBLISH_RETRY",
                        "outbox moved to DLQ after max retries"
                );
            } else {
                persistenceService.appendProcessingHistory(
                        outboxEvent.getAggregateId(),
                        ProcessingStatus.PUBLISH_FAILED,
                        "PUBLISH_RETRY",
                        safeMessage(ex)
                );
            }
        }
    }

    private void publishToDlq(OutboxEventEntity outboxEvent, Exception cause) {
        String reason = safeMessage(cause);
        String dlqPayload = "{"
                + "\"aggregateId\":" + quoteAsJsonString(outboxEvent.getAggregateId()) + ","
                + "\"eventType\":" + quoteAsJsonString(outboxEvent.getEventType()) + ","
                + "\"reason\":" + quoteAsJsonString(reason) + ","
                + "\"payload\":" + quoteAsJsonString(outboxEvent.getPayload())
                + "}";
        kafkaTemplate.send(dlqTopic, outboxEvent.getAggregateId(), dlqPayload).join();
    }

    private static String safeMessage(Exception ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private static String quoteAsJsonString(String raw) {
        if (raw == null) {
            return "null";
        }
        return "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

