package org.example.fidstp2.validator;

import org.example.fidstp2.domain.FxOptionTrade;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductTypeTradeValidatorRegistry {
    private final Map<String, ProductTypeTradeValidator> validatorsByProductType;

    public ProductTypeTradeValidatorRegistry(List<ProductTypeTradeValidator> validators) {
        List<ProductTypeTradeValidator> immutableValidators = List.copyOf(
                Objects.requireNonNull(validators, "validators are required")
        );
        if (immutableValidators.isEmpty()) {
            throw new IllegalArgumentException("at least one validator is required");
        }
        this.validatorsByProductType = immutableValidators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        validator -> ProductTypeNormalizer.normalize(validator.supportedProductType()),
                        Function.identity(),
                        (left, right) -> {
                            throw new IllegalArgumentException(
                                    "duplicate validator registration for productType: "
                                            + ProductTypeNormalizer.normalize(left.supportedProductType())
                            );
                        }
                ));
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
        String productType = ProductTypeNormalizer.normalize(trade.getProductType());
        ProductTypeTradeValidator validator = validatorsByProductType.get(productType);
        if (validator != null) {
            return validator.validate(trade);
        }
        return ValidationResult.invalid(List.of(new ValidationError(
                trade.getTradeId(),
                "VALIDATION",
                "PRODUCT_TYPE_UNSUPPORTED",
                "unsupported productType: " + productType,
                Instant.now()
        )));
    }
}

