package org.example.fidstp2.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FxOptionTrade extends Trade {
    private final OptionType optionType;
    private final BigDecimal strikePrice;
    private final LocalDate expiryDate;
    private final OptionStyle optionStyle;
    private final List<TradeLeg> legs;

    public FxOptionTrade(
            String tradeId,
            String externalTradeId,
            String productType,
            String currencyPair,
            BigDecimal notionalAmount,
            String notionalCurrency,
            BuySell buySell,
            LocalDate tradeDate,
            LocalDate valueDate,
            String counterpartyId,
            String legalEntityId,
            String sourceSystem,
            ProcessingStatus processingStatus,
            Instant receivedTimestamp,
            Instant processedTimestamp,
            OptionType optionType,
            BigDecimal strikePrice,
            LocalDate expiryDate,
            OptionStyle optionStyle,
            List<TradeLeg> legs
    ) {
        super(
                tradeId,
                externalTradeId,
                productType,
                currencyPair,
                notionalAmount,
                notionalCurrency,
                buySell,
                tradeDate,
                valueDate,
                counterpartyId,
                legalEntityId,
                sourceSystem,
                processingStatus,
                receivedTimestamp,
                processedTimestamp
        );
        this.optionType = Objects.requireNonNull(optionType, "optionType is required");
        if (strikePrice == null || strikePrice.signum() <= 0) {
            throw new IllegalArgumentException("strikePrice must be positive");
        }
        this.strikePrice = strikePrice;
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate is required");
        if (expiryDate.isBefore(getTradeDate())) {
            throw new IllegalArgumentException("expiryDate must be on or after tradeDate");
        }

        this.optionStyle = Objects.requireNonNull(optionStyle, "optionStyle is required");
        List<TradeLeg> safeLegs = legs == null ? List.of() : new ArrayList<>(legs);
        if (optionStyle == OptionStyle.MULTI_LEG && safeLegs.isEmpty()) {
            throw new IllegalArgumentException("multi-leg option must include at least one leg");
        }
        this.legs = List.copyOf(safeLegs);
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public OptionStyle getOptionStyle() {
        return optionStyle;
    }

    public List<TradeLeg> getLegs() {
        return legs;
    }
}

