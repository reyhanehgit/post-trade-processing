package org.example.fidstp2.service;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.mapper.EnrichedTradeToFxOptionTradeEntityMapper;
import org.example.fidstp2.persistence.FxOptionTradeEntity;
import org.example.fidstp2.persistence.OutboxEventEntity;
import org.example.fidstp2.persistence.ProcessingHistoryEntity;
import org.example.fidstp2.persistence.TradeEntity;
import org.example.fidstp2.repository.OutboxEventRepository;
import org.example.fidstp2.repository.ProcessingHistoryRepository;
import org.example.fidstp2.repository.TradeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class TradePersistenceService {
    private final TradeRepository tradeRepository;
    private final ProcessingHistoryRepository processingHistoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final EnrichedTradeToFxOptionTradeEntityMapper mapper;
    private final Clock clock;

    public TradePersistenceService(
            TradeRepository tradeRepository,
            ProcessingHistoryRepository processingHistoryRepository,
            OutboxEventRepository outboxEventRepository,
            EnrichedTradeToFxOptionTradeEntityMapper mapper,
            Clock clock
    ) {
        this.tradeRepository = Objects.requireNonNull(tradeRepository, "tradeRepository is required");
        this.processingHistoryRepository = Objects.requireNonNull(
                processingHistoryRepository,
                "processingHistoryRepository is required"
        );
        this.outboxEventRepository = Objects.requireNonNull(outboxEventRepository, "outboxEventRepository is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
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
        outbox.setCreatedAt(Instant.now(clock));
        return outboxEventRepository.save(outbox).getId();
    }

    @Transactional
    public void markOutboxEventPublished(Long outboxId) {
        outboxEventRepository.findById(outboxId).ifPresent(outbox -> {
            outbox.setStatus("PUBLISHED");
            outbox.setPublishedAt(Instant.now(clock));
            outboxEventRepository.save(outbox);
        });
    }
}

