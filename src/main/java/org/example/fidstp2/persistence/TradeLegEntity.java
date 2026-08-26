package org.example.fidstp2.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.fidstp2.domain.OptionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trade_leg")
public class TradeLegEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leg_id", nullable = false)
    private String legId;

    @Column(name = "currency_pair", nullable = false)
    private String currencyPair;

    @Column(name = "notional", nullable = false, precision = 19, scale = 4)
    private BigDecimal notional;

    @Column(name = "strike_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal strikePrice;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;

    @ManyToOne
    @JoinColumn(name = "trade_id", nullable = false)
    private FxOptionTradeEntity trade;

    protected TradeLegEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getLegId() {
        return legId;
    }

    public void setLegId(String legId) {
        this.legId = legId;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public void setNotional(BigDecimal notional) {
        this.notional = notional;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice = strikePrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public FxOptionTradeEntity getTrade() {
        return trade;
    }

    public void setTrade(FxOptionTradeEntity trade) {
        this.trade = trade;
    }
}

