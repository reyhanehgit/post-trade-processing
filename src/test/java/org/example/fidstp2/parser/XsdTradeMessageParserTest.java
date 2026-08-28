package org.example.fidstp2.parser;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.exception.TradeParsingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XsdTradeMessageParserTest {

    private final XsdTradeMessageParser parser = new XsdTradeMessageParser();

    @Test
    void parsesValidTradeEnvelopeXml() {
        FxOptionTrade trade = parser.parse(validFxOptionXml());

        assertEquals("T-700", trade.getTradeId());
        assertEquals("FX_OPTION", trade.getProductType());
        assertEquals("CP-7", trade.getCounterpartyId());
    }

    @Test
    void rejectsMalformedXml() {
        TradeParsingException ex = assertThrows(TradeParsingException.class, () -> parser.parse("<not-xml"));

        assertEquals("failed to parse trade xml", ex.getMessage());
    }

    private String validFxOptionXml() {
        return """
                <tradeEnvelope xmlns="http://example.org/fidstp2/trade" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
                <trade xsi:type="FxOptionTradeType">\
                <tradeId>T-700</tradeId><externalTradeId>EXT-700</externalTradeId><productType>FX_OPTION</productType>\
                <currencyPair>EUR/USD</currencyPair><notionalAmount>1200000</notionalAmount><notionalCurrency>EUR</notionalCurrency>\
                <buySell>BUY</buySell><tradeDate>2026-08-26</tradeDate><valueDate>2026-08-29</valueDate>\
                <optionType>CALL</optionType><strikePrice>1.1900</strikePrice><expiryDate>2026-10-01</expiryDate><optionStyle>VANILLA</optionStyle>\
                </trade>\
                <counterparty><counterpartyId>CP-7</counterpartyId><legalEntityId>LE-7</legalEntityId><sourceSystem>OMS</sourceSystem></counterparty>\
                </tradeEnvelope>
                """;
    }
}

