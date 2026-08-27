package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.domain.SettlementInstruction;
import org.example.fidstp2.exception.TradeEnrichmentException;

import java.util.Locale;
import java.util.Objects;

public class FxOptionTradeEnrichmentService implements ProductTypeTradeEnrichmentService {
    private final CounterpartyService counterpartyService;
    private final CurrencyPairService currencyPairService;
    private final LegalEntityService legalEntityService;
    private final SettlementInstructionService settlementInstructionService;

    public FxOptionTradeEnrichmentService(
            CounterpartyService counterpartyService,
            CurrencyPairService currencyPairService,
            LegalEntityService legalEntityService,
            SettlementInstructionService settlementInstructionService
    ) {
        this.counterpartyService = Objects.requireNonNull(counterpartyService, "counterpartyService is required");
        this.currencyPairService = Objects.requireNonNull(currencyPairService, "currencyPairService is required");
        this.legalEntityService = Objects.requireNonNull(legalEntityService, "legalEntityService is required");
        this.settlementInstructionService = Objects.requireNonNull(
                settlementInstructionService,
                "settlementInstructionService is required"
        );
    }

    @Override
    public boolean supports(String productType) {
        if (productType == null || productType.isBlank()) {
            return true;
        }
        String normalized = productType.toUpperCase(Locale.ROOT);
        return "FX_OPTION".equals(normalized) || "OPTION".equals(normalized);
    }

    @Override
    public EnrichedTrade enrich(FxOptionTrade trade) {
        try {
            Counterparty counterparty = counterpartyService.getCounterparty(trade.getCounterpartyId());
            CurrencyPair currencyPair = currencyPairService.getCurrencyPair(trade.getCurrencyPair());
            LegalEntity legalEntity = legalEntityService.getLegalEntity(trade.getLegalEntityId());
            SettlementInstruction settlementInstruction = settlementInstructionService.getSettlementInstruction(
                    trade.getTradeId(),
                    trade.getCurrencyPair(),
                    trade.getCounterpartyId()
            );

            return new EnrichedTrade(trade, counterparty, currencyPair, legalEntity, settlementInstruction);
        } catch (RuntimeException ex) {
            throw new TradeEnrichmentException("failed to enrich trade " + trade.getTradeId(), ex);
        }
    }
}

