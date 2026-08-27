package org.example.fidstp2.validator;

import org.example.fidstp2.domain.FxOptionTrade;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductTypeTradeValidatorRegistry {
    private final List<ProductTypeTradeValidator> validators;

    public ProductTypeTradeValidatorRegistry(List<ProductTypeTradeValidator> validators) {
        this.validators = List.copyOf(Objects.requireNonNull(validators, "validators are required"));
        if (this.validators.isEmpty()) {
            throw new IllegalArgumentException("at least one validator is required");
        }
    }

    public ValidationResult validate(FxOptionTrade trade) {
        if (trade == null) {
            return ValidationResult.invalid(List.of(new ValidationError(
                    null,
                    "VALIDATION",
                    "TRADE_NULL",
                    "trade payload is required",
                    Instant.now()
            )));
        }
        String productType = normalizeProductType(trade.getProductType());
        for (ProductTypeTradeValidator validator : validators) {
            if (validator.supports(productType)) {
                return validator.validate(trade);
            }
        }
        return ValidationResult.invalid(List.of(new ValidationError(
                trade.getTradeId(),
                "VALIDATION",
                "PRODUCT_TYPE_UNSUPPORTED",
                "unsupported productType: " + productType,
                Instant.now()
        )));
    }

    private static String normalizeProductType(String rawProductType) {
        if (rawProductType == null || rawProductType.isBlank()) {
            return "FX_OPTION";
        }
        String value = rawProductType.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

