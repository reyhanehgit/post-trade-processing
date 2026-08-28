package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.schema.trade.CounterpartyType;
import org.example.fidstp2.schema.trade.FxOptionTradeType;
import org.example.fidstp2.schema.trade.TradeEnvelopeType;
import org.example.fidstp2.schema.trade.TradeType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class XsdFxOptionTradeMapper {

    public FxOptionTrade toDomain(TradeEnvelopeType envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("trade envelope is required");
        }
        TradeType trade = envelope.getTrade();
        if (!(trade instanceof FxOptionTradeType fxOptionTrade)) {
            throw new IllegalArgumentException("tradeEnvelope.trade must be FxOptionTradeType for this mapper");
        }
        CounterpartyType counterparty = envelope.getCounterparty();
        if (counterparty == null) {
            throw new IllegalArgumentException("tradeEnvelope.counterparty is required");
        }

        return new FxOptionTrade(
                fxOptionTrade.getTradeId(),
                fxOptionTrade.getExternalTradeId(),
                fxOptionTrade.getProductType().value(),
                fxOptionTrade.getCurrencyPair(),
                fxOptionTrade.getNotionalAmount(),
                fxOptionTrade.getNotionalCurrency(),
                BuySell.valueOf(fxOptionTrade.getBuySell().value()),
                toLocalDate(fxOptionTrade.getTradeDate()),
                toLocalDate(fxOptionTrade.getValueDate()),
                counterparty.getCounterpartyId(),
                counterparty.getLegalEntityId(),
                counterparty.getSourceSystem(),
                ProcessingStatus.RECEIVED,
                Instant.now(),
                null,
                OptionType.valueOf(fxOptionTrade.getOptionType().value()),
                fxOptionTrade.getStrikePrice(),
                toLocalDate(fxOptionTrade.getExpiryDate()),
                OptionStyle.valueOf(fxOptionTrade.getOptionStyle().value()),
                List.of()
        );
    }

    private static LocalDate toLocalDate(XMLGregorianCalendar xmlDate) {
        if (xmlDate == null) {
            throw new IllegalArgumentException("xml date is required");
        }
        return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }
}

