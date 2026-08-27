package org.example.fidstp2.consumer;

import org.example.fidstp2.service.TradePipelineService;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "app.kafka.listener.enabled", havingValue = "true", matchIfMissing = true)
public class FxOptionTradeConsumer {
    private final TradePipelineService tradePipelineService;

    public FxOptionTradeConsumer(TradePipelineService tradePipelineService) {
        this.tradePipelineService = Objects.requireNonNull(tradePipelineService, "tradePipelineService is required");
    }

    @KafkaListener(topics = "${app.kafka.inbound-topic}", groupId = "${spring.kafka.consumer.group-id:fidstp2-consumer}")
    public void onMessage(String rawMessage) {
        tradePipelineService.handleRawMessage(rawMessage);
    }
}

