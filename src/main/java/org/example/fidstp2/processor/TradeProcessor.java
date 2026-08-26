package org.example.fidstp2.processor;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.domain.Trade;

public interface TradeProcessor<T extends Trade> {
    ProcessedTrade process(T trade);
}

