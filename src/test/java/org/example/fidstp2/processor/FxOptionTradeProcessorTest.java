package org.example.fidstp2.processor;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.domain.SettlementInstruction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxOptionTradeProcessorTest {

    @Test
    void processesEnrichedTradeToProcessedStatus() {
        FxOptionTrade trade = validTrade();
        EnrichedTrade enrichedTrade = new EnrichedTrade(
                trade,
                new Counterparty("CP-1", "Bank A", true),
                new CurrencyPair("EUR/USD", "EUR", "USD"),
                new LegalEntity("LE-1", "Entity A", "EMEA"),
                new SettlementInstruction("SI-1", "ACC-001", "CLS")
        );

        FxOptionTradeProcessor processor = new FxOptionTradeProcessor();
        ProcessedTrade result = processor.process(enrichedTrade);

        assertEquals("T-400", result.getTradeId());
        assertEquals(ProcessingStatus.PROCESSED, result.getStatus());
        assertTrue(result.getProcessingNotes().stream().anyMatch(note -> note.contains("Counterparty: CP-1")));
    }

    private FxOptionTrade validTrade() {
        return new FxOptionTrade(
                "T-400",
                "EXT-400",
                "FX_OPTION",
                "EUR/USD",
                new BigDecimal("1000000"),
                "EUR",
                BuySell.BUY,
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 29),
                "CP-1",
                "LE-1",
                "OMS",
                ProcessingStatus.RECEIVED,
                Instant.parse("2026-08-26T10:00:00Z"),
                null,
                OptionType.CALL,
                new BigDecimal("1.1234"),
                LocalDate.of(2026, 10, 1),
                OptionStyle.VANILLA,
                List.of()
        );
    }
}

