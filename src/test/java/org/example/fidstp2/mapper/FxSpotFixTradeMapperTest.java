package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.dto.FixMessageDto;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxSpotFixTradeMapperTest {

    private final FxSpotFixTradeMapper mapper = new FxSpotFixTradeMapper(new FixToFxOptionTradeMapper());

    @Test
    void supportsOnlySpotProductType() {
        assertTrue(mapper.supports("SPOT"));
        assertFalse(mapper.supports("FX_OPTION"));
        assertFalse(mapper.supports("OPTION"));
    }

    @Test
    void mapsSpotMessageUsingDelegate() {
        FixMessageDto dto = new FixMessageDto(Map.ofEntries(
                Map.entry("11", "T-SPOT-1"),
                Map.entry("37", "EXT-SPOT-1"),
                Map.entry("20000", "SPOT"),
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

        assertEquals("T-SPOT-1", trade.getTradeId());
        assertEquals("SPOT", trade.getProductType());
    }
}
