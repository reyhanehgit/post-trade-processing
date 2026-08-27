package org.example.fidstp2.validator;

import org.example.fidstp2.domain.FxOptionTrade;

public interface ProductTypeTradeValidator extends TradeValidator<FxOptionTrade> {
    boolean supports(String productType);
}

