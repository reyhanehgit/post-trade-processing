package org.example.fidstp2.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.Objects;

public class ReliabilityMetrics {
    private final Counter outboxRetryAttemptCounter;
    private final Counter outboxDeadLetterCounter;
    private final Counter outboxRetrySuccessCounter;
    private final Counter consumerDlqCounter;
    private final Timer counterpartyCachePreloadTimer;
    private final DistributionSummary counterpartyCachePreloadRecords;
    private final Timer currencyPairCachePreloadTimer;
    private final DistributionSummary currencyPairCachePreloadRecords;

    public ReliabilityMetrics(MeterRegistry meterRegistry) {
        Objects.requireNonNull(meterRegistry, "meterRegistry is required");
        this.outboxRetryAttemptCounter = meterRegistry.counter("fidstp2.outbox.retry.attempts");
        this.outboxDeadLetterCounter = meterRegistry.counter("fidstp2.outbox.dead_lettered");
        this.outboxRetrySuccessCounter = meterRegistry.counter("fidstp2.outbox.retry.success");
        this.consumerDlqCounter = meterRegistry.counter("fidstp2.consumer.dlq.publishes");
        this.counterpartyCachePreloadTimer = meterRegistry.timer("fidstp2.enrichment.counterparty.cache.preload.duration");
        this.counterpartyCachePreloadRecords = meterRegistry.summary("fidstp2.enrichment.counterparty.cache.preload.records");
        this.currencyPairCachePreloadTimer = meterRegistry.timer("fidstp2.enrichment.currency_pair.cache.preload.duration");
        this.currencyPairCachePreloadRecords = meterRegistry.summary("fidstp2.enrichment.currency_pair.cache.preload.records");
    }

    public void incrementOutboxRetryAttempt() {
        outboxRetryAttemptCounter.increment();
    }

    public void incrementOutboxDeadLettered() {
        outboxDeadLetterCounter.increment();
    }

    public void incrementOutboxRetrySuccess() {
        outboxRetrySuccessCounter.increment();
    }

    public void incrementConsumerDlqPublish() {
        consumerDlqCounter.increment();
    }

    public void recordCounterpartyCachePreload(int loadedRecords, Duration duration) {
        counterpartyCachePreloadRecords.record(Math.max(0, loadedRecords));
        counterpartyCachePreloadTimer.record(Objects.requireNonNull(duration, "duration is required"));
    }

    public void recordCurrencyPairCachePreload(int loadedRecords, Duration duration) {
        currencyPairCachePreloadRecords.record(Math.max(0, loadedRecords));
        currencyPairCachePreloadTimer.record(Objects.requireNonNull(duration, "duration is required"));
    }
}

