package org.example.fidstp2.service;

import org.example.fidstp2.domain.EnrichedTrade;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.enrichment.TradeEnrichmentService;
import org.example.fidstp2.exception.TradeValidationException;
import org.example.fidstp2.parser.TradeMessageParser;
import org.example.fidstp2.processor.FxOptionTradeProcessor;
import org.example.fidstp2.validator.TradeValidator;
import org.example.fidstp2.validator.ValidationError;
import org.example.fidstp2.validator.ValidationResult;

import java.util.Objects;
import java.util.stream.Collectors;

public class TradeProcessingService {
    private final TradeMessageParser<FxOptionTrade> parser;
    private final TradeValidator<FxOptionTrade> validator;
    private final TradeEnrichmentService<FxOptionTrade> enrichmentService;
    private final FxOptionTradeProcessor processor;
    private final TradePersistenceService persistenceService;

    public TradeProcessingService(
            TradeMessageParser<FxOptionTrade> parser,
            TradeValidator<FxOptionTrade> validator,
            TradeEnrichmentService<FxOptionTrade> enrichmentService,
            FxOptionTradeProcessor processor,
            TradePersistenceService persistenceService
    ) {
        this.parser = Objects.requireNonNull(parser, "parser is required");
        this.validator = Objects.requireNonNull(validator, "validator is required");
        this.enrichmentService = Objects.requireNonNull(enrichmentService, "enrichmentService is required");
        this.processor = Objects.requireNonNull(processor, "processor is required");
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService is required");
    }

    public ProcessedTrade processRawMessage(String rawMessage) {
        FxOptionTrade trade = parser.parse(rawMessage);
        try {
            ValidationResult validationResult = validator.validate(trade);
            if (!validationResult.isValid()) {
                throw new TradeValidationException(toValidationMessage(validationResult));
            }

            EnrichedTrade enrichedTrade = enrichmentService.enrich(trade);
            persistenceService.upsertTrade(enrichedTrade);

            ProcessedTrade processedTrade = processor.process(enrichedTrade);
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

