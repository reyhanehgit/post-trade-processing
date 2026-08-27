package org.example.fidstp2.processor;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.ProcessedTrade;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductTypeTradeProcessorRegistry {
    private final List<ProductTypeTradeProcessor> processors;

    public ProductTypeTradeProcessorRegistry(List<ProductTypeTradeProcessor> processors) {
        this.processors = List.copyOf(Objects.requireNonNull(processors, "processors are required"));
        if (this.processors.isEmpty()) {
            throw new IllegalArgumentException("at least one processor is required");
        }
    }

    public ProcessedTrade process(EnrichedTrade enrichedTrade) {
        Objects.requireNonNull(enrichedTrade, "enrichedTrade is required");
        String productType = normalizeProductType(enrichedTrade.trade().getProductType());
        for (ProductTypeTradeProcessor processor : processors) {
            if (processor.supports(productType)) {
                return processor.process(enrichedTrade);
            }
        }
        throw new IllegalStateException("unsupported productType for processor: " + productType);
    }

    private static String normalizeProductType(String rawProductType) {
        if (rawProductType == null || rawProductType.isBlank()) {
            return "FX_OPTION";
        }
        String value = rawProductType.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

