package org.example.fidstp2.service;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.mapper.EnrichedTradeToFxOptionTradeEntityMapper;
import org.example.fidstp2.persistence.FxOptionTradeEntity;
import org.example.fidstp2.persistence.OutboxEventEntity;
import org.example.fidstp2.persistence.OutboxEventStatus;
import org.example.fidstp2.persistence.ProcessingHistoryEntity;
import org.example.fidstp2.persistence.TradeEntity;
import org.example.fidstp2.repository.OutboxEventRepository;
import org.example.fidstp2.repository.ProcessingHistoryRepository;
import org.example.fidstp2.repository.TradeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class TradePersistenceService {
    private final TradeRepository tradeRepository;
    private final ProcessingHistoryRepository processingHistoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final EnrichedTradeToFxOptionTradeEntityMapper mapper;
    private final Clock clock;
    private final ReliabilityMetrics reliabilityMetrics;

    public TradePersistenceService(
            TradeRepository tradeRepository,
            ProcessingHistoryRepository processingHistoryRepository,
            OutboxEventRepository outboxEventRepository,
            EnrichedTradeToFxOptionTradeEntityMapper mapper,
            Clock clock,
            ReliabilityMetrics reliabilityMetrics
    ) {
        this.tradeRepository = Objects.requireNonNull(tradeRepository, "tradeRepository is required");
        this.processingHistoryRepository = Objects.requireNonNull(
                processingHistoryRepository,
                "processingHistoryRepository is required"
        );
        this.outboxEventRepository = Objects.requireNonNull(outboxEventRepository, "outboxEventRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
        this.reliabilityMetrics = Objects.requireNonNull(reliabilityMetrics, "reliabilityMetrics is required");
    }

    @Transactional
    public void upsertTrade(EnrichedTrade enrichedTrade) {
        String tradeId = enrichedTrade.trade().getTradeId();
        TradeEntity existing = tradeRepository.findByTradeId(tradeId).orElse(null);

        FxOptionTradeEntity entity;
        if (existing == null) {
            entity = mapper.toNewEntity(enrichedTrade);
        } else if (existing instanceof FxOptionTradeEntity fxOptionTradeEntity) {
            mapper.apply(enrichedTrade, fxOptionTradeEntity);
            entity = fxOptionTradeEntity;
        } else {
            throw new IllegalStateException("unsupported existing trade type for tradeId=" + tradeId);
        }

        tradeRepository.save(entity);
    }

    @Transactional
    public void markProcessed(ProcessedTrade processedTrade) {
        tradeRepository.findByTradeId(processedTrade.getTradeId()).ifPresent(entity -> {
            entity.setProcessingStatus(processedTrade.getStatus());
            entity.setProcessedTimestamp(processedTrade.getProcessedAt());
            tradeRepository.save(entity);
        });
    }

    @Transactional
    public void appendProcessingHistory(String tradeId, ProcessingStatus status, String stage, String message) {
        ProcessingHistoryEntity history = new ProcessingHistoryEntity();
        history.setTradeId(tradeId);
        history.setStatus(status);
        history.setStage(stage);
        history.setMessage(message);
        history.setCreatedAt(Instant.now(clock));
        processingHistoryRepository.save(history);
    }

    @Transactional
    public Long createOutboxEvent(String aggregateId, String eventType, String payload, String status) {
        OutboxEventEntity outbox = new OutboxEventEntity();
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setPayload(payload);
        outbox.setStatus(status);
        Instant now = Instant.now(clock);
        outbox.setCreatedAt(now);
        outbox.setRetryCount(0);
        outbox.setNextRetryAt(now);
        return outboxEventRepository.save(outbox).getId();
    }

    @Transactional
    public void markOutboxEventPublished(Long outboxId) {
        outboxEventRepository.findById(outboxId).ifPresent(outbox -> {
            outbox.setStatus(OutboxEventStatus.PUBLISHED.value());
            outbox.setPublishedAt(Instant.now(clock));
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxEventRepository.save(outbox);
        });
    }

    @Transactional
    public boolean markOutboxEventForRetry(Long outboxId, String errorMessage, int maxAttempts, Duration baseBackoff) {
        return outboxEventRepository.findById(outboxId).map(outbox -> {
            Instant now = Instant.now(clock);
            int currentRetryCount = outbox.getRetryCount() + 1;
            outbox.setRetryCount(currentRetryCount);
            reliabilityMetrics.incrementOutboxRetryAttempt();
            outbox.setLastError(truncateError(errorMessage));
            outbox.setFailedAt(now);

            if (currentRetryCount >= maxAttempts) {
                outbox.setStatus(OutboxEventStatus.DEAD_LETTERED.value());
                outbox.setNextRetryAt(null);
                reliabilityMetrics.incrementOutboxDeadLettered();
                outboxEventRepository.save(outbox);
                return true;
            }

            long backoffMultiplier = 1L << Math.max(0, currentRetryCount - 1);
            outbox.setStatus(OutboxEventStatus.PENDING_RETRY.value());
            outbox.setNextRetryAt(now.plus(baseBackoff.multipliedBy(backoffMultiplier)));
            outboxEventRepository.save(outbox);
            return false;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<OutboxEventEntity> findDueOutboxEventsForRetry(int batchSize) {
        return outboxEventRepository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAscCreatedAtAsc(
                OutboxEventStatus.PENDING_RETRY.value(),
                Instant.now(clock),
                PageRequest.of(0, batchSize)
        );
    }

    private static String truncateError(String errorMessage) {
        String normalized = errorMessage == null ? "unknown publish error" : errorMessage;
        if (normalized.length() <= 1000) {
            return normalized;
        }
        return normalized.substring(0, 1000);
    }
}

