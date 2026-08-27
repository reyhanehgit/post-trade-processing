package org.example.fidstp2.service;

import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.domain.SettlementInstruction;
import org.example.fidstp2.enrichment.FxOptionTradeEnrichmentService;
import org.example.fidstp2.enrichment.InMemoryCounterpartyService;
import org.example.fidstp2.enrichment.InMemoryCurrencyPairService;
import org.example.fidstp2.enrichment.InMemoryLegalEntityService;
import org.example.fidstp2.enrichment.InMemorySettlementInstructionService;
import org.example.fidstp2.exception.TradeValidationException;
import org.example.fidstp2.parser.FixTradeMessageParser;
import org.example.fidstp2.processor.FxOptionTradeProcessor;
import org.example.fidstp2.validator.FxOptionTradeValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TradeProcessingServiceTest {

    @Test
    void processesRawFixMessageEndToEnd() {
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        TradeProcessingService service = buildService(persistenceService);

        ProcessedTrade result = service.processRawMessage(validFixMessage());

        assertEquals("T-500", result.getTradeId());
        assertEquals(ProcessingStatus.PROCESSED, result.getStatus());
        verify(persistenceService).upsertTrade(any());
        verify(persistenceService).markProcessed(any());
        verify(persistenceService).appendProcessingHistory(eq("T-500"), eq(ProcessingStatus.PROCESSED), eq("PROCESSING"), any());
    }

    @Test
    void rejectsInvalidTradeDuringValidation() {
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        TradeProcessingService service = buildService(persistenceService);

        TradeValidationException ex = assertThrows(
                TradeValidationException.class,
                () -> service.processRawMessage(invalidProductTypeMessage())
        );

        assertEquals("PRODUCT_TYPE_INVALID: productType must be FX_OPTION or OPTION", ex.getMessage());
        verify(persistenceService).appendProcessingHistory(eq("T-501"), eq(ProcessingStatus.VALIDATION_FAILED), eq("VALIDATION"), any());
    }

    private TradeProcessingService buildService(TradePersistenceService persistenceService) {
        return new TradeProcessingService(
                new FixTradeMessageParser(),
                new FxOptionTradeValidator(),
                new FxOptionTradeEnrichmentService(
                        new InMemoryCounterpartyService(Map.of("CP-1", new Counterparty("CP-1", "Bank A", true))),
                        new InMemoryCurrencyPairService(Map.of("EUR/USD", new CurrencyPair("EUR/USD", "EUR", "USD"))),
                        new InMemoryLegalEntityService(Map.of("LE-1", new LegalEntity("LE-1", "Entity A", "EMEA"))),
                        new InMemorySettlementInstructionService(Map.of(
                                "CP-1",
                                new SettlementInstruction("SI-1", "ACC-001", "CLS")
                        ))
                ),
                new FxOptionTradeProcessor(),
                persistenceService
        );
    }

    private String validFixMessage() {
        return "11=T-500|37=EXT-500|20000=OPTION|55=EUR/USD|15=EUR|38=2500000|54=1|75=20260826|64=20260829|"
                + "20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-1|20006=LE-1|49=OMS|";
    }

    private String invalidProductTypeMessage() {
        return "11=T-501|37=EXT-501|20000=SPOT|55=EUR/USD|15=EUR|38=2500000|54=1|75=20260826|64=20260829|"
                + "20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-1|20006=LE-1|49=OMS|";
    }
}

