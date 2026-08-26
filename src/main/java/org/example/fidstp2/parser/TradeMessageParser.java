package org.example.fidstp2.parser;

import org.example.fidstp2.domain.Trade;

public interface TradeMessageParser<T extends Trade> {
    T parse(String rawMessage);
}

