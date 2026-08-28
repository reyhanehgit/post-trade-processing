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
import org.example.fidstp2.enrichment.ProductTypeTradeEnrichmentRegistry;
import org.example.fidstp2.enrichment.SpotTradeEnrichmentService;
import org.example.fidstp2.exception.TradeValidationException;
import org.example.fidstp2.parser.XsdTradeMessageParser;
import org.example.fidstp2.processor.FxOptionTradeProcessor;
import org.example.fidstp2.processor.ProductTypeTradeProcessorRegistry;
import org.example.fidstp2.processor.SpotTradeProcessor;
import org.example.fidstp2.validator.FxOptionTradeValidator;
import org.example.fidstp2.validator.ProductTypeTradeValidatorRegistry;
import org.example.fidstp2.validator.SpotTradeValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
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

        assertEquals("PRODUCT_TYPE_UNSUPPORTED: unsupported productType: FORWARD", ex.getMessage());
        verify(persistenceService).appendProcessingHistory(eq("T-501"), eq(ProcessingStatus.VALIDATION_FAILED), eq("VALIDATION"), any());
    }

    @Test
    void processesSpotTradeThroughSpotStrategies() {
        TradePersistenceService persistenceService = mock(TradePersistenceService.class);
        TradeProcessingService service = buildService(persistenceService);

        ProcessedTrade result = service.processRawMessage(validSpotMessage());

        assertEquals("T-502", result.getTradeId());
        assertEquals(ProcessingStatus.PROCESSED, result.getStatus());
        verify(persistenceService).appendProcessingHistory(eq("T-502"), eq(ProcessingStatus.PROCESSED), eq("PROCESSING"), any());
    }

    private TradeProcessingService buildService(TradePersistenceService persistenceService) {
        FxOptionTradeEnrichmentService fxOptionEnrichment = new FxOptionTradeEnrichmentService(
                new InMemoryCounterpartyService(Map.of("CP-1", new Counterparty("CP-1", "Bank A", true))),
                new InMemoryCurrencyPairService(Map.of("EUR/USD", new CurrencyPair("EUR/USD", "EUR", "USD"))),
                new InMemoryLegalEntityService(Map.of("LE-1", new LegalEntity("LE-1", "Entity A", "EMEA"))),
                new InMemorySettlementInstructionService(Map.of(
                        "CP-1",
                        new SettlementInstruction("SI-1", "ACC-001", "CLS")
                ))
        );
        FxOptionTradeProcessor fxOptionProcessor = new FxOptionTradeProcessor();

        return new TradeProcessingService(
                new XsdTradeMessageParser(),
                new ProductTypeTradeValidatorRegistry(List.of(new SpotTradeValidator(), new FxOptionTradeValidator())),
                new ProductTypeTradeEnrichmentRegistry(List.of(new SpotTradeEnrichmentService(fxOptionEnrichment), fxOptionEnrichment)),
                new ProductTypeTradeProcessorRegistry(List.of(new SpotTradeProcessor(fxOptionProcessor), fxOptionProcessor)),
                persistenceService
        );
    }

    private String validFixMessage() {
        return """
                <tradeEnvelope xmlns=\"http://example.org/fidstp2/trade\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\
                <trade xsi:type=\"FxOptionTradeType\">\
                <tradeId>T-500</tradeId><externalTradeId>EXT-500</externalTradeId><productType>FX_OPTION</productType>\
                <currencyPair>EUR/USD</currencyPair><notionalAmount>2500000</notionalAmount><notionalCurrency>EUR</notionalCurrency>\
                <buySell>BUY</buySell><tradeDate>2026-08-26</tradeDate><valueDate>2026-08-29</valueDate>\
                <optionType>CALL</optionType><strikePrice>1.2500</strikePrice><expiryDate>2026-10-01</expiryDate><optionStyle>VANILLA</optionStyle>\
                </trade>\
                <counterparty><counterpartyId>CP-1</counterpartyId><legalEntityId>LE-1</legalEntityId><sourceSystem>OMS</sourceSystem></counterparty>\
                </tradeEnvelope>
                """;
    }

    private String invalidProductTypeMessage() {
        return """
                <tradeEnvelope xmlns=\"http://example.org/fidstp2/trade\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\
                <trade xsi:type=\"FxOptionTradeType\">\
                <tradeId>T-501</tradeId><externalTradeId>EXT-501</externalTradeId><productType>FORWARD</productType>\
                <currencyPair>EUR/USD</currencyPair><notionalAmount>2500000</notionalAmount><notionalCurrency>EUR</notionalCurrency>\
                <buySell>BUY</buySell><tradeDate>2026-08-26</tradeDate><valueDate>2026-08-29</valueDate>\
                <optionType>CALL</optionType><strikePrice>1.2500</strikePrice><expiryDate>2026-10-01</expiryDate><optionStyle>VANILLA</optionStyle>\
                </trade>\
                <counterparty><counterpartyId>CP-1</counterpartyId><legalEntityId>LE-1</legalEntityId><sourceSystem>OMS</sourceSystem></counterparty>\
                </tradeEnvelope>
                """;
    }

    private String validSpotMessage() {
        return """
                <tradeEnvelope xmlns=\"http://example.org/fidstp2/trade\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\
                <trade xsi:type=\"FxOptionTradeType\">\
                <tradeId>T-502</tradeId><externalTradeId>EXT-502</externalTradeId><productType>SPOT</productType>\
                <currencyPair>EUR/USD</currencyPair><notionalAmount>1500000</notionalAmount><notionalCurrency>EUR</notionalCurrency>\
                <buySell>BUY</buySell><tradeDate>2026-08-26</tradeDate><valueDate>2026-08-29</valueDate>\
                <optionType>CALL</optionType><strikePrice>1.2200</strikePrice><expiryDate>2026-10-01</expiryDate><optionStyle>VANILLA</optionStyle>\
                </trade>\
                <counterparty><counterpartyId>CP-1</counterpartyId><legalEntityId>LE-1</legalEntityId><sourceSystem>OMS</sourceSystem></counterparty>\
                </tradeEnvelope>
                """;
    }
}

