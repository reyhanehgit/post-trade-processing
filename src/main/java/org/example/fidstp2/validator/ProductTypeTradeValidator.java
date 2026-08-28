package org.example.fidstp2.validator;

import org.example.fidstp2.domain.FxOptionTrade;

public interface ProductTypeTradeValidator extends TradeValidator<FxOptionTrade> {
    String supportedProductType();

    default boolean supports(String productType) {
        return ProductTypeNormalizer.normalize(productType)
                .equals(ProductTypeNormalizer.normalize(supportedProductType()));
    }
}

