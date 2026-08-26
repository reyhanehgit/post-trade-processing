package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.domain.SettlementInstruction;
import org.example.fidstp2.exception.TradeEnrichmentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FxOptionTradeEnrichmentServiceTest {

    @Test
    void enrichesTradeWithReferenceData() {
        FxOptionTradeEnrichmentService service = new FxOptionTradeEnrichmentService(
                new InMemoryCounterpartyService(Map.of("CP-1", new Counterparty("CP-1", "Bank A", true))),
                new InMemoryCurrencyPairService(Map.of("EUR/USD", new CurrencyPair("EUR/USD", "EUR", "USD"))),
                new InMemoryLegalEntityService(Map.of("LE-1", new LegalEntity("LE-1", "Entity A", "EMEA"))),
                new InMemorySettlementInstructionService(Map.of(
                        "CP-1",
                        new SettlementInstruction("SI-1", "ACC-001", "CLS")
                ))
        );

        EnrichedTrade enrichedTrade = service.enrich(validTrade());

        assertEquals("CP-1", enrichedTrade.counterparty().id());
        assertEquals("EUR/USD", enrichedTrade.currencyPair().symbol());
        assertEquals("LE-1", enrichedTrade.legalEntity().id());
        assertEquals("SI-1", enrichedTrade.settlementInstruction().id());
    }

    @Test
    void wrapsMissingReferenceDataAsEnrichmentException() {
        FxOptionTradeEnrichmentService service = new FxOptionTradeEnrichmentService(
                new InMemoryCounterpartyService(Map.of()),
                new InMemoryCurrencyPairService(Map.of("EUR/USD", new CurrencyPair("EUR/USD", "EUR", "USD"))),
                new InMemoryLegalEntityService(Map.of("LE-1", new LegalEntity("LE-1", "Entity A", "EMEA"))),
                new InMemorySettlementInstructionService(Map.of())
        );

        assertThrows(TradeEnrichmentException.class, () -> service.enrich(validTrade()));
    }

    private FxOptionTrade validTrade() {
        return new FxOptionTrade(
                "T-300",
                "EXT-300",
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

