package org.example.fidstp2.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class TradeLeg {
    private final String legId;
    private final String currencyPair;
    private final BigDecimal notional;
    private final BigDecimal strikePrice;
    private final LocalDate expiryDate;
    private final OptionType optionType;

    public TradeLeg(
            String legId,
            String currencyPair,
            BigDecimal notional,
            BigDecimal strikePrice,
            LocalDate expiryDate,
            OptionType optionType
    ) {
        if (legId == null || legId.isBlank()) {
            throw new IllegalArgumentException("legId is required");
        }
        if (currencyPair == null || currencyPair.isBlank()) {
            throw new IllegalArgumentException("currencyPair is required");
        }
        if (notional == null || notional.signum() <= 0) {
            throw new IllegalArgumentException("notional must be positive");
        }
        if (strikePrice == null || strikePrice.signum() <= 0) {
            throw new IllegalArgumentException("strikePrice must be positive");
        }
        this.legId = legId;
        this.currencyPair = currencyPair;
        this.notional = notional;
        this.strikePrice = strikePrice;
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate is required");
        this.optionType = Objects.requireNonNull(optionType, "optionType is required");
    }

    public String getLegId() {
        return legId;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public OptionType getOptionType() {
        return optionType;
    }
}

