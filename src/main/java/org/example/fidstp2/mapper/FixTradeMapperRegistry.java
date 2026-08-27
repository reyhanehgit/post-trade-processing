package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.Trade;
import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.exception.TradeParsingException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FixTradeMapperRegistry<T extends Trade> {
    private final List<FixProductTradeMapper<? extends T>> mappers;

    public FixTradeMapperRegistry(List<FixProductTradeMapper<? extends T>> mappers) {
        this.mappers = List.copyOf(Objects.requireNonNull(mappers, "mappers are required"));
        if (this.mappers.isEmpty()) {
            throw new IllegalArgumentException("at least one mapper is required");
        }
    }

    public T map(FixMessageDto fixMessage) {
        Objects.requireNonNull(fixMessage, "fixMessage is required");
        String productType = normalizeProductType(fixMessage.get("20000"));
        for (FixProductTradeMapper<? extends T> mapper : mappers) {
            if (mapper.supports(productType)) {
                return mapper.map(fixMessage);
            }
        }
        throw new TradeParsingException("unsupported productType: " + productType);
    }

    private static String normalizeProductType(String rawProductType) {
        if (rawProductType == null || rawProductType.isBlank()) {
            return "FX_OPTION";
        }
        String value = rawProductType.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

