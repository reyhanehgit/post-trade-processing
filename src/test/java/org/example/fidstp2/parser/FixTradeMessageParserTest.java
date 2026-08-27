package org.example.fidstp2.parser;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.exception.TradeParsingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixTradeMessageParserTest {

    private final FixTradeMessageParser parser = new FixTradeMessageParser();

    @Test
    void parsesValidFixMessage() {
        String raw = "11=T-100|37=EXT-100|20000=OPTION|55=EUR/USD|15=EUR|38=2500000|54=1|75=20260826|64=20260829|"
                + "20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-101|20006=LE-42|49=OMS|";

        FxOptionTrade trade = parser.parse(raw);

        assertEquals("T-100", trade.getTradeId());
        assertEquals("EXT-100", trade.getExternalTradeId());
        assertEquals("FX_OPTION", trade.getProductType());
        assertEquals(BuySell.BUY, trade.getBuySell());
        assertEquals(OptionType.CALL, trade.getOptionType());
        assertEquals(OptionStyle.VANILLA, trade.getOptionStyle());
        assertEquals("EUR", trade.getBaseCurrency());
        assertEquals("USD", trade.getQuoteCurrency());
        assertEquals("CP-101", trade.getCounterpartyId());
        assertEquals("LE-42", trade.getLegalEntityId());
    }

    @Test
    void rejectsUnknownSideValue() {
        String raw = "11=T-100|37=EXT-100|20000=OPTION|55=EUR/USD|15=EUR|38=2500000|54=9|75=20260826|"
                + "20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-101|20006=LE-42|49=OMS|";

        TradeParsingException ex = assertThrows(TradeParsingException.class, () -> parser.parse(raw));
        assertEquals("unsupported side tag 54 value: 9", ex.getMessage());
    }

    @Test
    void parsesSohDelimitedMessage() {
        String raw = "11=T-101\u000137=EXT-101\u000120000=OPTION\u000155=EUR/USD\u000115=USD\u000138=1000\u000154=2\u000175=20260826\u0001"
                + "20001=PUT\u000144=1.1100\u000120003=20260926\u000120004=VANILLA\u00011=CP-102\u000120006=LE-43\u000149=EMS\u0001";

        FxOptionTrade trade = parser.parse(raw);

        assertEquals("T-101", trade.getTradeId());
        assertEquals(BuySell.SELL, trade.getBuySell());
        assertEquals(OptionType.PUT, trade.getOptionType());
    }

    @Test
    void parsesSpotProductTypeUsingRegistryRouting() {
        String raw = "11=T-102|37=EXT-102|20000=SPOT|55=EUR/USD|15=EUR|38=2500000|54=1|75=20260826|64=20260829|"
                + "20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-101|20006=LE-42|49=OMS|";

        FxOptionTrade trade = parser.parse(raw);

        assertEquals("T-102", trade.getTradeId());
        assertEquals("SPOT", trade.getProductType());
    }
}

