package org.example.fidstp2.parser;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.mapper.FixToFxOptionTradeMapper;

import java.util.Objects;

public class FixTradeMessageParser implements TradeMessageParser<FxOptionTrade> {
    private final FixMessageAdapter adapter;
    private final FixToFxOptionTradeMapper mapper;

    public FixTradeMessageParser() {
        this(new DefaultFixMessageAdapter(), new FixToFxOptionTradeMapper());
    }

    public FixTradeMessageParser(FixMessageAdapter adapter, FixToFxOptionTradeMapper mapper) {
        this.adapter = Objects.requireNonNull(adapter, "adapter is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public FxOptionTrade parse(String rawMessage) {
        FixMessageDto fixMessage = adapter.adapt(rawMessage);
        return mapper.map(fixMessage);
    }
}

