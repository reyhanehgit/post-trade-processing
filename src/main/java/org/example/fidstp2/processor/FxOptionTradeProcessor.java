package org.example.fidstp2.processor;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FxOptionTradeProcessor implements TradeProcessor<FxOptionTrade>, ProductTypeTradeProcessor {
    @Override
    public boolean supports(String productType) {
        if (productType == null || productType.isBlank()) {
            return true;
        }
        String normalized = productType.toUpperCase(Locale.ROOT);
        return "FX_OPTION".equals(normalized) || "OPTION".equals(normalized);
    }

    @Override
    public ProcessedTrade process(FxOptionTrade trade) {
        List<String> notes = new ArrayList<>();
        notes.add("Processed FX option trade");
        notes.add("Option type: " + trade.getOptionType());
        notes.add("Style: " + trade.getOptionStyle());

        return new ProcessedTrade(
                trade.getTradeId(),
                ProcessingStatus.PROCESSED,
                Instant.now(),
                notes
        );
    }

    @Override
    public ProcessedTrade process(EnrichedTrade enrichedTrade) {
        List<String> notes = new ArrayList<>();
        notes.add("Processed enriched FX option trade");
        notes.add("Counterparty: " + enrichedTrade.counterparty().id());
        notes.add("Legal entity: " + enrichedTrade.legalEntity().id());
        notes.add("Settlement instruction: " + enrichedTrade.settlementInstruction().id());

        return new ProcessedTrade(
                enrichedTrade.trade().getTradeId(),
                ProcessingStatus.PROCESSED,
                Instant.now(),
                notes
        );
    }
}

