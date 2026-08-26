package org.example.fidstp2.consumer;

import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.service.TradePipelineService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FxOptionTradeConsumerTest {

    @Test
    void delegatesRawMessageToPipeline() {
        TradePipelineService tradePipelineService = mock(TradePipelineService.class);
        FxOptionTradeConsumer consumer = new FxOptionTradeConsumer(tradePipelineService);
        ProcessedTradeEvent event = new ProcessedTradeEvent("1.0", "T-1", "PROCESSED", Instant.now());

        when(tradePipelineService.handleRawMessage("raw-fix")).thenReturn(event);

        consumer.onMessage("raw-fix");

        verify(tradePipelineService).handleRawMessage("raw-fix");
    }
}

