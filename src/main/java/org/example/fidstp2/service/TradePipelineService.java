package org.example.fidstp2.service;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.dto.ProcessedTradeEvent;

import java.util.Objects;

public class TradePipelineService {
    private final TradeProcessingService tradeProcessingService;
    private final TradePublicationService tradePublicationService;

    public TradePipelineService(
            TradeProcessingService tradeProcessingService,
            TradePublicationService tradePublicationService
    ) {
        this.tradeProcessingService = Objects.requireNonNull(tradeProcessingService, "tradeProcessingService is required");
        this.tradePublicationService = Objects.requireNonNull(tradePublicationService, "tradePublicationService is required");
    }

    public ProcessedTradeEvent handleRawMessage(String rawMessage) {
        ProcessedTrade processedTrade = tradeProcessingService.processRawMessage(rawMessage);
        return tradePublicationService.publish(processedTrade);
    }
}

