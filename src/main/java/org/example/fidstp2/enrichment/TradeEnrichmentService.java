package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.Trade;

public interface TradeEnrichmentService<T extends Trade> {
    EnrichedTrade enrich(T trade);
}

