package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.dto.FixMessageDto;

import java.util.Objects;

public class FxSpotFixTradeMapper implements FixProductTradeMapper<FxOptionTrade> {
    private final FixToFxOptionTradeMapper delegate;

    public FxSpotFixTradeMapper(FixToFxOptionTradeMapper delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public boolean supports(String productType) {
        return "SPOT".equals(productType);
    }

    @Override
    public FxOptionTrade map(FixMessageDto fixMessage) {
        // Reuse existing field mapping while the downstream pipeline remains FxOptionTrade-based.
        return delegate.map(fixMessage);
    }
}

