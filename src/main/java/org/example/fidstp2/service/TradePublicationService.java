package org.example.fidstp2.service;

import org.example.fidstp2.domain.ProcessedTrade;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.mapper.ProcessedTradeToPublishedEventMapper;
import org.example.fidstp2.publisher.PublishedEventPublisher;

import java.util.Objects;

public class TradePublicationService {
    private final ProcessedTradeToPublishedEventMapper mapper;
    private final PublishedEventPublisher<ProcessedTradeEvent> publisher;

    public TradePublicationService(
            ProcessedTradeToPublishedEventMapper mapper,
            PublishedEventPublisher<ProcessedTradeEvent> publisher
    ) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.publisher = Objects.requireNonNull(publisher, "publisher is required");
    }

    public ProcessedTradeEvent publish(ProcessedTrade processedTrade) {
        ProcessedTradeEvent event = mapper.map(processedTrade);
        publisher.publish(processedTrade.getTradeId(), event);
        return event;
    }
}

