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

    public TradeProcessingService(
            TradeMessageParser<FxOptionTrade> parser,
            TradeValidator<FxOptionTrade> validator,
            TradeEnrichmentService<FxOptionTrade> enrichmentService,
            FxOptionTradeProcessor processor
    ) {
        this.parser = Objects.requireNonNull(parser, "parser is required");
        this.validator = Objects.requireNonNull(validator, "validator is required");
        this.enrichmentService = Objects.requireNonNull(enrichmentService, "enrichmentService is required");
        this.processor = Objects.requireNonNull(processor, "processor is required");
    }

    public ProcessedTrade processRawMessage(String rawMessage) {
        FxOptionTrade trade = parser.parse(rawMessage);
        ValidationResult validationResult = validator.validate(trade);
        if (!validationResult.isValid()) {
            throw new TradeValidationException(toValidationMessage(validationResult));
        }

        EnrichedTrade enrichedTrade = enrichmentService.enrich(trade);
        return processor.process(enrichedTrade);
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

