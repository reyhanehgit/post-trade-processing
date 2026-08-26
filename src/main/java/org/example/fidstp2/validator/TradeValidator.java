package org.example.fidstp2.validator;

import org.example.fidstp2.domain.Trade;

public interface TradeValidator<T extends Trade> {
    ValidationResult validate(T trade);
}

