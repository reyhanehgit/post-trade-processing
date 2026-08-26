package org.example.fidstp2.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FxOptionTradeContractTest {

    @Test
    void createsVanillaTradeWithDerivedCurrencies() {
        FxOptionTrade trade = new FxOptionTrade(
                "T-1",
                "EXT-1",
                "FX_OPTION",
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

        assertEquals("EUR", trade.getBaseCurrency());
        assertEquals("USD", trade.getQuoteCurrency());
    }

    @Test
    void rejectsNegativeStrikePrice() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new FxOptionTrade(
                "T-2",
                "EXT-2",
                "FX_OPTION",
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
                new BigDecimal("-1.1234"),
                LocalDate.of(2026, 10, 1),
                OptionStyle.VANILLA,
                List.of()
        ));

        assertEquals("strikePrice must be positive", ex.getMessage());
    }

    @Test
    void requiresLegsForMultiLegStyle() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new FxOptionTrade(
                "T-3",
                "EXT-3",
                "FX_OPTION",
                "EUR/USD",
                new BigDecimal("1000000"),
                "EUR",
                BuySell.SELL,
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 29),
                "CP-1",
                "LE-1",
                "OMS",
                ProcessingStatus.RECEIVED,
                Instant.parse("2026-08-26T10:00:00Z"),
                null,
                OptionType.PUT,
                new BigDecimal("1.1000"),
                LocalDate.of(2026, 10, 1),
                OptionStyle.MULTI_LEG,
                List.of()
        ));

        assertEquals("multi-leg option must include at least one leg", ex.getMessage());
    }
}

