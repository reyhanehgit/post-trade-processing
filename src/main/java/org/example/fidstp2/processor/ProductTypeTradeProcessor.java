package org.example.fidstp2.processor;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.ProcessedTrade;

public interface ProductTypeTradeProcessor {
    boolean supports(String productType);

    ProcessedTrade process(EnrichedTrade enrichedTrade);
}

