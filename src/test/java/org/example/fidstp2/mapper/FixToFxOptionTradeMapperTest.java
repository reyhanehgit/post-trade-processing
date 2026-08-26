package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.exception.TradeParsingException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixToFxOptionTradeMapperTest {

    private final FixToFxOptionTradeMapper mapper = new FixToFxOptionTradeMapper();

    @Test
    void mapsFixDtoIntoDomainTrade() {
        FixMessageDto dto = new FixMessageDto(Map.ofEntries(
                Map.entry("11", "T-700"),
                Map.entry("37", "EXT-700"),
                Map.entry("20000", "OPTION"),
                Map.entry("55", "EUR/USD"),
                Map.entry("15", "EUR"),
                Map.entry("38", "2000000"),
                Map.entry("54", "1"),
                Map.entry("75", "20260826"),
                Map.entry("64", "20260829"),
                Map.entry("20001", "CALL"),
                Map.entry("44", "1.3300"),
                Map.entry("20003", "20261130"),
                Map.entry("20004", "VANILLA"),
                Map.entry("1", "CP-1"),
                Map.entry("20006", "LE-1"),
                Map.entry("49", "OMS")
        ));

        FxOptionTrade trade = mapper.map(dto);

        assertEquals("T-700", trade.getTradeId());
        assertEquals("EXT-700", trade.getExternalTradeId());
        assertEquals("FX_OPTION", trade.getProductType());
        assertEquals(BuySell.BUY, trade.getBuySell());
        assertEquals(OptionType.CALL, trade.getOptionType());
    }

    @Test
    void failsWhenRequiredTagMissing() {
        FixMessageDto dto = new FixMessageDto(Map.of("55", "EUR/USD"));

        TradeParsingException ex = assertThrows(TradeParsingException.class, () -> mapper.map(dto));
        assertEquals("missing FIX tag 75 for tradeDate", ex.getMessage());
    }
}

