package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.Trade;
import org.example.fidstp2.domain.TradeLeg;
import org.example.fidstp2.persistence.FxOptionTradeEntity;
import org.example.fidstp2.persistence.TradeEntity;
import org.example.fidstp2.persistence.TradeLegEntity;

import java.util.ArrayList;
import java.util.List;

public class EnrichedTradeToFxOptionTradeEntityMapper {

    public FxOptionTradeEntity toNewEntity(EnrichedTrade enrichedTrade) {
        FxOptionTradeEntity entity = new FxOptionTradeEntity();
        apply(enrichedTrade, entity);
        return entity;
    }

    public void apply(EnrichedTrade enrichedTrade, FxOptionTradeEntity target) {
        FxOptionTrade trade = enrichedTrade.trade();
        mapTradeFields(trade, target);
        target.setOptionType(trade.getOptionType());
        target.setStrikePrice(trade.getStrikePrice());
        target.setExpiryDate(trade.getExpiryDate());
        target.setOptionStyle(trade.getOptionStyle());

        // Replace legs atomically to keep JPA orphan-removal behavior predictable.
        target.getLegs().clear();
        target.getLegs().addAll(mapLegs(trade.getLegs(), target));
    }

    private static void mapTradeFields(Trade source, TradeEntity target) {
        target.setTradeId(source.getTradeId());
        target.setExternalTradeId(source.getExternalTradeId());
        target.setProductType(source.getProductType());
        target.setCurrencyPair(source.getCurrencyPair());
        target.setBaseCurrency(source.getBaseCurrency());
        target.setQuoteCurrency(source.getQuoteCurrency());
        target.setNotionalAmount(source.getNotionalAmount());
        target.setNotionalCurrency(source.getNotionalCurrency());
        target.setBuySell(source.getBuySell());
        target.setTradeDate(source.getTradeDate());
        target.setValueDate(source.getValueDate());
        target.setCounterpartyId(source.getCounterpartyId());
        target.setLegalEntityId(source.getLegalEntityId());
        target.setSourceSystem(source.getSourceSystem());
        target.setProcessingStatus(source.getProcessingStatus());
        target.setReceivedTimestamp(source.getReceivedTimestamp());
        target.setProcessedTimestamp(source.getProcessedTimestamp());
    }

    private static List<TradeLegEntity> mapLegs(List<TradeLeg> sourceLegs, FxOptionTradeEntity parent) {
        List<TradeLegEntity> result = new ArrayList<>();
        for (TradeLeg sourceLeg : sourceLegs) {
            TradeLegEntity leg = new TradeLegEntity();
            leg.setLegId(sourceLeg.getLegId());
            leg.setCurrencyPair(sourceLeg.getCurrencyPair());
            leg.setNotional(sourceLeg.getNotional());
            leg.setStrikePrice(sourceLeg.getStrikePrice());
            leg.setExpiryDate(sourceLeg.getExpiryDate());
            leg.setOptionType(sourceLeg.getOptionType());
            leg.setTrade(parent);
            result.add(leg);
        }
        return result;
    }
}

