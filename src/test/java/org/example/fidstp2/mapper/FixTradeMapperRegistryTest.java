package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.FixMessageDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixTradeMapperRegistryTest {

    @Test
    void usesFirstSupportingMapper() {
        FxOptionTrade specificTrade = sampleTrade("T-SPECIFIC");
        FxOptionTrade fallbackTrade = sampleTrade("T-FALLBACK");

        FixProductTradeMapper<FxOptionTrade> specificMapper = new StubMapper("FX_OPTION", specificTrade);
        FixProductTradeMapper<FxOptionTrade> fallbackMapper = new StubMapper("*", fallbackTrade);

        FixTradeMapperRegistry<FxOptionTrade> registry = new FixTradeMapperRegistry<>(List.of(specificMapper, fallbackMapper));

        FxOptionTrade mapped = registry.map(new FixMessageDto(Map.of("20000", "OPTION")));

        assertEquals("T-SPECIFIC", mapped.getTradeId());
    }

    @Test
    void fallsBackWhenNoSpecificMapperSupportsProductType() {
        FxOptionTrade fallbackTrade = sampleTrade("T-FALLBACK");

        FixProductTradeMapper<FxOptionTrade> specificMapper = new StubMapper("FX_OPTION", sampleTrade("T-SPECIFIC"));
        FixProductTradeMapper<FxOptionTrade> fallbackMapper = new StubMapper("*", fallbackTrade);

        FixTradeMapperRegistry<FxOptionTrade> registry = new FixTradeMapperRegistry<>(List.of(specificMapper, fallbackMapper));

        FxOptionTrade mapped = registry.map(new FixMessageDto(Map.of("20000", "SPOT")));

        assertEquals("T-FALLBACK", mapped.getTradeId());
    }

    private static FxOptionTrade sampleTrade(String tradeId) {
        return new FxOptionTrade(
                tradeId,
                "EXT-" + tradeId,
                "FX_OPTION",
                "EUR/USD",
                BigDecimal.valueOf(1_000_000),
                "EUR",
                BuySell.BUY,
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 29),
                "CP-1",
                "LE-1",
                "OMS",
                ProcessingStatus.RECEIVED,
                Instant.parse("2026-08-26T00:00:00Z"),
                null,
                OptionType.CALL,
                BigDecimal.valueOf(1.25),
                LocalDate.of(2026, 10, 1),
                OptionStyle.VANILLA,
                List.of()
        );
    }

    private static final class StubMapper implements FixProductTradeMapper<FxOptionTrade> {
        private final String supportedType;
        private final FxOptionTrade trade;

        private StubMapper(String supportedType, FxOptionTrade trade) {
            this.supportedType = supportedType;
            this.trade = trade;
        }

        @Override
        public boolean supports(String productType) {
            return "*".equals(supportedType) || supportedType.equals(productType);
        }

        @Override
        public FxOptionTrade map(FixMessageDto fixMessage) {
            return trade;
        }
    }
}

