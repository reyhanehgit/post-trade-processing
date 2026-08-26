package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.SettlementInstruction;

public interface SettlementInstructionService {
    SettlementInstruction getSettlementInstruction(String tradeId, String currencyPair, String counterpartyId);
}

