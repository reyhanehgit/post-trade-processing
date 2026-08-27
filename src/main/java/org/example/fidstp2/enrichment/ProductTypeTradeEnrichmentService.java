package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.FxOptionTrade;

public interface ProductTypeTradeEnrichmentService extends TradeEnrichmentService<FxOptionTrade> {
    boolean supports(String productType);
}

