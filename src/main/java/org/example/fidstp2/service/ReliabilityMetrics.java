package org.example.fidstp2.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Objects;

public class ReliabilityMetrics {
    private final Counter outboxRetryAttemptCounter;
    private final Counter outboxDeadLetterCounter;
    private final Counter outboxRetrySuccessCounter;
    private final Counter consumerDlqCounter;

    public ReliabilityMetrics(MeterRegistry meterRegistry) {
        Objects.requireNonNull(meterRegistry, "meterRegistry is required");
        this.outboxRetryAttemptCounter = meterRegistry.counter("fidstp2.outbox.retry.attempts");
        this.outboxDeadLetterCounter = meterRegistry.counter("fidstp2.outbox.dead_lettered");
        this.outboxRetrySuccessCounter = meterRegistry.counter("fidstp2.outbox.retry.success");
        this.consumerDlqCounter = meterRegistry.counter("fidstp2.consumer.dlq.publishes");
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
}

