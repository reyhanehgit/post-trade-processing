package org.example.fidstp2.processor;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FxOptionTradeProcessor implements TradeProcessor<FxOptionTrade> {
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

