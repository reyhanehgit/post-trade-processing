package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.Trade;
import org.example.fidstp2.dto.FixMessageDto;

public interface FixProductTradeMapper<T extends Trade> {
    boolean supports(String productType);

    T map(FixMessageDto fixMessage);
}

