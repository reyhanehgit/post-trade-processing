package org.example.fidstp2.parser;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.mapper.FixTradeMapperRegistry;
import org.example.fidstp2.mapper.FixToFxOptionTradeMapper;
import org.example.fidstp2.mapper.FxSpotFixTradeMapper;

import java.util.List;
import java.util.Objects;

public class FixTradeMessageParser implements TradeMessageParser<FxOptionTrade> {
    private final FixMessageAdapter adapter;
    private final FixTradeMapperRegistry<FxOptionTrade> mapperRegistry;

    public FixTradeMessageParser() {
        this(
                new DefaultFixMessageAdapter(),
                defaultRegistry()
        );
    }

    public FixTradeMessageParser(FixMessageAdapter adapter, FixTradeMapperRegistry<FxOptionTrade> mapperRegistry) {
        this.adapter = Objects.requireNonNull(adapter, "adapter is required");
        this.mapperRegistry = Objects.requireNonNull(mapperRegistry, "mapperRegistry is required");
    }

    private static FixTradeMapperRegistry<FxOptionTrade> defaultRegistry() {
        FixToFxOptionTradeMapper fallbackMapper = new FixToFxOptionTradeMapper();
        return new FixTradeMapperRegistry<>(List.of(new FxSpotFixTradeMapper(fallbackMapper), fallbackMapper));
    }

    @Override
    public FxOptionTrade parse(String rawMessage) {
        FixMessageDto fixMessage = adapter.adapt(rawMessage);
        return mapperRegistry.map(fixMessage);
    }
}

