package org.example.fidstp2.service;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.enrichment.ProductTypeTradeEnrichmentRegistry;
import org.example.fidstp2.exception.TradeValidationException;
import org.example.fidstp2.parser.TradeMessageParser;
import org.example.fidstp2.processor.ProductTypeTradeProcessorRegistry;
import org.example.fidstp2.validator.ProductTypeTradeValidatorRegistry;
import org.example.fidstp2.validator.ValidationError;
import org.example.fidstp2.validator.ValidationResult;

import java.util.Objects;
import java.util.stream.Collectors;

public class TradeProcessingService {
    private final TradeMessageParser<FxOptionTrade> parser;
    private final ProductTypeTradeValidatorRegistry validatorRegistry;
    private final ProductTypeTradeEnrichmentRegistry enrichmentRegistry;
    private final ProductTypeTradeProcessorRegistry processorRegistry;
    private final TradePersistenceService persistenceService;

    public TradeProcessingService(
            TradeMessageParser<FxOptionTrade> parser,
            ProductTypeTradeValidatorRegistry validatorRegistry,
            ProductTypeTradeEnrichmentRegistry enrichmentRegistry,
            ProductTypeTradeProcessorRegistry processorRegistry,
            TradePersistenceService persistenceService
    ) {
        this.parser = Objects.requireNonNull(parser, "parser is required");
        this.validatorRegistry = Objects.requireNonNull(validatorRegistry, "validatorRegistry is required");
        this.enrichmentRegistry = Objects.requireNonNull(enrichmentRegistry, "enrichmentRegistry is required");
        this.processorRegistry = Objects.requireNonNull(processorRegistry, "processorRegistry is required");
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService is required");
    }

    public ProcessedTrade processRawMessage(String rawMessage) {
        FxOptionTrade trade = parser.parse(rawMessage);
        try {
            ValidationResult validationResult = validatorRegistry.validate(trade);
            if (!validationResult.isValid()) {
                throw new TradeValidationException(toValidationMessage(validationResult));
            }

            EnrichedTrade enrichedTrade = enrichmentRegistry.enrich(trade);
            persistenceService.upsertTrade(enrichedTrade);

            ProcessedTrade processedTrade = processorRegistry.process(enrichedTrade);
            persistenceService.markProcessed(processedTrade);
            persistenceService.appendProcessingHistory(
                    processedTrade.getTradeId(),
                    processedTrade.getStatus(),
                    "PROCESSING",
                    toProcessingMessage(processedTrade)
            );
            return processedTrade;
        } catch (TradeValidationException ex) {
            persistenceService.appendProcessingHistory(
                    trade.getTradeId(),
                    org.example.fidstp2.domain.ProcessingStatus.VALIDATION_FAILED,
                    "VALIDATION",
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            persistenceService.appendProcessingHistory(
                    trade.getTradeId(),
                    org.example.fidstp2.domain.ProcessingStatus.PROCESSING_FAILED,
                    "PROCESSING",
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()
            );
            throw ex;
        }
    }

    private static String toProcessingMessage(ProcessedTrade processedTrade) {
        if (processedTrade.getProcessingNotes().isEmpty()) {
            return "processed";
        }
        return String.join("; ", processedTrade.getProcessingNotes());
    }

    private static String toValidationMessage(ValidationResult validationResult) {
        return validationResult.getErrors().stream()
                .map(TradeProcessingService::formatError)
                .collect(Collectors.joining("; "));
    }

    private static String formatError(ValidationError error) {
        return error.errorCode() + ": " + error.message();
    }
}

