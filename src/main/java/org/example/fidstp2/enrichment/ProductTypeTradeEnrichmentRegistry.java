package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.exception.TradeEnrichmentException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductTypeTradeEnrichmentRegistry {
    private final List<ProductTypeTradeEnrichmentService> enrichmentServices;

    public ProductTypeTradeEnrichmentRegistry(List<ProductTypeTradeEnrichmentService> enrichmentServices) {
        this.enrichmentServices = List.copyOf(Objects.requireNonNull(enrichmentServices, "enrichmentServices are required"));
        if (this.enrichmentServices.isEmpty()) {
            throw new IllegalArgumentException("at least one enrichment service is required");
        }
    }

    public EnrichedTrade enrich(FxOptionTrade trade) {
        Objects.requireNonNull(trade, "trade is required");
        String productType = normalizeProductType(trade.getProductType());
        for (ProductTypeTradeEnrichmentService enrichmentService : enrichmentServices) {
            if (enrichmentService.supports(productType)) {
                return enrichmentService.enrich(trade);
            }
        }
        throw new TradeEnrichmentException(
                "unsupported productType for enrichment: " + productType,
                new IllegalStateException("no enrichment strategy for " + productType)
        );
    }

    private static String normalizeProductType(String rawProductType) {
        if (rawProductType == null || rawProductType.isBlank()) {
            return "FX_OPTION";
        }
        String value = rawProductType.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

