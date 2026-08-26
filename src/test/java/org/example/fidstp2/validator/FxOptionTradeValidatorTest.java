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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxOptionTradeValidatorTest {

    private final FxOptionTradeValidator validator = new FxOptionTradeValidator();

    @Test
    void returnsValidForWellFormedTrade() {
        ValidationResult result = validator.validate(validTrade("FX_OPTION", "EUR"));

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void flagsInvalidProductType() {
        ValidationResult result = validator.validate(validTrade("SPOT", "EUR"));

        assertFalse(result.isValid());
        assertEquals("PRODUCT_TYPE_INVALID", result.getErrors().get(0).errorCode());
    }

    @Test
    void flagsInvalidNotionalCurrency() {
        ValidationResult result = validator.validate(validTrade("FX_OPTION", "EURO"));

        assertFalse(result.isValid());
        assertEquals("NOTIONAL_CCY_INVALID", result.getErrors().get(0).errorCode());
    }

    private FxOptionTrade validTrade(String productType, String notionalCurrency) {
        return new FxOptionTrade(
                "T-200",
                "EXT-200",
                productType,
                "EUR/USD",
                new BigDecimal("1000000"),
                notionalCurrency,
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
}

