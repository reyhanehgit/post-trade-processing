package org.example.fidstp2.service;

import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradePipelineServiceTest {

    @Test
    void delegatesProcessingThenPublication() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        TradePublicationService publicationService = mock(TradePublicationService.class);
        TradePipelineService pipelineService = new TradePipelineService(processingService, publicationService);

        ProcessedTradeEvent expectedEvent = new ProcessedTradeEvent("1.0", "T-1000", "PROCESSED", java.time.Instant.now());
        when(processingService.processRawMessage("raw-fix")).thenReturn(new org.example.fidstp2.domain.ProcessedTrade(
                "T-1000",
                org.example.fidstp2.domain.ProcessingStatus.PROCESSED,
                java.time.Instant.now(),
                java.util.List.of()
        ));
        when(publicationService.publish(org.mockito.ArgumentMatchers.any())).thenReturn(expectedEvent);

        ProcessedTradeEvent actualEvent = pipelineService.handleRawMessage("raw-fix");

        assertEquals(expectedEvent.tradeId(), actualEvent.tradeId());
    }
}

