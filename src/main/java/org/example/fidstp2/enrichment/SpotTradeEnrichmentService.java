package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;

import java.util.Objects;

public class SpotTradeEnrichmentService implements ProductTypeTradeEnrichmentService {
    private final FxOptionTradeEnrichmentService delegate;

    public SpotTradeEnrichmentService(FxOptionTradeEnrichmentService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public boolean supports(String productType) {
        return "SPOT".equals(productType);
    }

    @Override
    public EnrichedTrade enrich(FxOptionTrade trade) {
        return delegate.enrich(trade);
    }
}

