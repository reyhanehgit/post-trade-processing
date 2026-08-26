package org.example.fidstp2.domain;

import java.util.Objects;

public record EnrichedTrade(
        FxOptionTrade trade,
        Counterparty counterparty,
        CurrencyPair currencyPair,
        LegalEntity legalEntity,
        SettlementInstruction settlementInstruction
) {
    public EnrichedTrade {
        Objects.requireNonNull(trade, "trade is required");
        Objects.requireNonNull(counterparty, "counterparty is required");
        Objects.requireNonNull(currencyPair, "currencyPair is required");
        Objects.requireNonNull(legalEntity, "legalEntity is required");
        Objects.requireNonNull(settlementInstruction, "settlementInstruction is required");
    }
}

