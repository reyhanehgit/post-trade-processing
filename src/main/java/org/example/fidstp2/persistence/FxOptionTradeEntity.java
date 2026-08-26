package org.example.fidstp2.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("FX_OPTION")
public class FxOptionTradeEntity extends TradeEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;

    @Column(name = "strike_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal strikePrice;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_style", nullable = false)
    private OptionStyle optionStyle;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeLegEntity> legs = new ArrayList<>();

    protected FxOptionTradeEntity() {
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
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

    public OptionStyle getOptionStyle() {
        return optionStyle;
    }

    public void setOptionStyle(OptionStyle optionStyle) {
        this.optionStyle = optionStyle;
    }

    public List<TradeLegEntity> getLegs() {
        return legs;
    }

    public void setLegs(List<TradeLegEntity> legs) {
        this.legs = (legs == null) ? new ArrayList<>() : legs;
    }
}
