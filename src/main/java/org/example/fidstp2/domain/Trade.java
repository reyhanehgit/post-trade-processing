package org.example.fidstp2.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Trade {
    private final String tradeId;
    private final String externalTradeId;
    private final String productType;
    private final String currencyPair;
    private final String baseCurrency;
    private final String quoteCurrency;
    private final BigDecimal notionalAmount;
    private final String notionalCurrency;
    private final BuySell buySell;
    private final LocalDate tradeDate;
    private final LocalDate valueDate;
    private final String counterpartyId;
    private final String legalEntityId;
    private final String sourceSystem;
    private final ProcessingStatus processingStatus;
    private final Instant receivedTimestamp;
    private final Instant processedTimestamp;

    protected Trade(
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
            Instant processedTimestamp
    ) {
        this.tradeId = requireText(tradeId, "tradeId");
        this.externalTradeId = requireText(externalTradeId, "externalTradeId");
        this.productType = requireText(productType, "productType");
        this.currencyPair = requireText(currencyPair, "currencyPair");

        String[] currencies = currencyPair.split("/");
        if (currencies.length != 2 || currencies[0].length() != 3 || currencies[1].length() != 3) {
            throw new IllegalArgumentException("currencyPair must be formatted as AAA/BBB");
        }
        this.baseCurrency = currencies[0];
        this.quoteCurrency = currencies[1];

        this.notionalAmount = requirePositive(notionalAmount, "notionalAmount");
        this.notionalCurrency = requireText(notionalCurrency, "notionalCurrency");
        this.buySell = Objects.requireNonNull(buySell, "buySell is required");
        this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate is required");
        this.valueDate = Objects.requireNonNull(valueDate, "valueDate is required");
        if (valueDate.isBefore(tradeDate)) {
            throw new IllegalArgumentException("valueDate must be on or after tradeDate");
        }

        this.counterpartyId = requireText(counterpartyId, "counterpartyId");
        this.legalEntityId = requireText(legalEntityId, "legalEntityId");
        this.sourceSystem = requireText(sourceSystem, "sourceSystem");
        this.processingStatus = Objects.requireNonNull(processingStatus, "processingStatus is required");
        this.receivedTimestamp = Objects.requireNonNull(receivedTimestamp, "receivedTimestamp is required");
        this.processedTimestamp = processedTimestamp;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getExternalTradeId() {
        return externalTradeId;
    }

    public String getProductType() {
        return productType;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getNotionalAmount() {
        return notionalAmount;
    }

    public String getNotionalCurrency() {
        return notionalCurrency;
    }

    public BuySell getBuySell() {
        return buySell;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public String getCounterpartyId() {
        return counterpartyId;
    }

    public String getLegalEntityId() {
        return legalEntityId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public Instant getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public Instant getProcessedTimestamp() {
        return processedTimestamp;
    }
}

