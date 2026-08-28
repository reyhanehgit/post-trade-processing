package org.example.fidstp2.validator;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTypeTradeValidatorRegistryTest {

    @Test
    void routesSpotTradesUsingSupportedTypeLookup() {
        ProductTypeTradeValidatorRegistry registry = new ProductTypeTradeValidatorRegistry(List.of(
                new StubValidator("SPOT", "SPOT_VALIDATOR"),
                new StubValidator("FX_OPTION", "FX_OPTION_VALIDATOR")
        ));

        ValidationResult result = registry.validate(validTrade("SPOT"));

        assertEquals(false, result.isValid());
        assertEquals("SPOT_VALIDATOR", result.getErrors().get(0).errorCode());
    }

    @Test
    void routesOptionAliasToFxOptionValidator() {
        ProductTypeTradeValidatorRegistry registry = new ProductTypeTradeValidatorRegistry(List.of(
                new StubValidator("SPOT", "SPOT_VALIDATOR"),
                new StubValidator("FX_OPTION", "FX_OPTION_VALIDATOR")
        ));

        ValidationResult result = registry.validate(validTrade("OPTION"));

        assertEquals(false, result.isValid());
        assertEquals("FX_OPTION_VALIDATOR", result.getErrors().get(0).errorCode());
    }

    @Test
    void rejectsDuplicateNormalizedValidatorKeysAtStartup() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ProductTypeTradeValidatorRegistry(List.of(
                        new StubValidator("OPTION", "FIRST"),
                        new StubValidator("FX_OPTION", "SECOND")
                ))
        );

        assertEquals("duplicate validator registration for productType: FX_OPTION", ex.getMessage());
    }

    private FxOptionTrade validTrade(String productType) {
        return new FxOptionTrade(
                "T-200",
                "EXT-200",
                productType,
                "EUR/USD",
                new BigDecimal("1000000"),
                "EUR",
                BuySell.BUY,
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 29),
                "CP-1",
                "LE-1",
                "OMS",
                ProcessingStatus.RECEIVED,
                Instant.parse("2026-08-26T10:00:00Z"),
                null,
                OptionType.CALL,
                new BigDecimal("1.1234"),
                LocalDate.of(2026, 10, 1),
                OptionStyle.VANILLA,
                List.of()
        );
    }

    private static final class StubValidator implements ProductTypeTradeValidator {
        private final String supportedProductType;
        private final String code;

        private StubValidator(String supportedProductType, String code) {
            this.supportedProductType = supportedProductType;
            this.code = code;
        }

        @Override
        public String supportedProductType() {
            return supportedProductType;
        }

        @Override
        public ValidationResult validate(FxOptionTrade trade) {
            return ValidationResult.invalid(List.of(new ValidationError(
                    trade.getTradeId(),
                    "VALIDATION",
                    code,
                    "selected",
                    Instant.parse("2026-08-26T10:00:00Z")
            )));
        }
    }
}

