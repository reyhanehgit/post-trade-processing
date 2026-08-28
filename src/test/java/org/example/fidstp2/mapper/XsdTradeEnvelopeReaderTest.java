package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.schema.trade.TradeEnvelopeType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XsdTradeEnvelopeReaderTest {

    @Test
    void readsSchemaBasedXmlAndMapsToDomainTrade() {
        XsdTradeEnvelopeReader reader = new XsdTradeEnvelopeReader();
        XsdFxOptionTradeMapper mapper = new XsdFxOptionTradeMapper();

        InputStream xml = getClass().getResourceAsStream("/schema/fx-option-trade.xml");
        TradeEnvelopeType envelope = reader.read(xml);
        FxOptionTrade trade = mapper.toDomain(envelope);

        assertNotNull(trade);
        assertEquals("T-900", trade.getTradeId());
        assertEquals("FX_OPTION", trade.getProductType());
        assertEquals("CP-1", trade.getCounterpartyId());
        assertEquals("LE-1", trade.getLegalEntityId());
        assertEquals("OMS", trade.getSourceSystem());
        assertEquals("EUR/USD", trade.getCurrencyPair());
    }
}

