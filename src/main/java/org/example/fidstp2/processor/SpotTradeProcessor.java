package org.example.fidstp2.processor;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.ProcessedTrade;

import java.util.Objects;

public class SpotTradeProcessor implements ProductTypeTradeProcessor {
    private final FxOptionTradeProcessor delegate;

    public SpotTradeProcessor(FxOptionTradeProcessor delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public boolean supports(String productType) {
        return "SPOT".equals(productType);
    }

    @Override
    public ProcessedTrade process(EnrichedTrade enrichedTrade) {
        return delegate.process(enrichedTrade);
    }
}

