package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.SettlementInstruction;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;

import java.util.Map;

public class InMemorySettlementInstructionService implements SettlementInstructionService {
    private final Map<String, SettlementInstruction> instructionsByCounterparty;

    public InMemorySettlementInstructionService(Map<String, SettlementInstruction> instructionsByCounterparty) {
        this.instructionsByCounterparty = Map.copyOf(instructionsByCounterparty);
    }

    @Override
    public SettlementInstruction getSettlementInstruction(String tradeId, String currencyPair, String counterpartyId) {
        SettlementInstruction instruction = instructionsByCounterparty.get(counterpartyId);
        if (instruction == null) {
            throw new ReferenceDataNotFoundException(
                    "settlement instruction not found for counterparty " + counterpartyId + " on trade " + tradeId
            );
        }
        return instruction;
    }
}

