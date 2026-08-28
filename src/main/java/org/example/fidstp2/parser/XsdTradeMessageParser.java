package org.example.fidstp2.parser;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.exception.TradeParsingException;
import org.example.fidstp2.mapper.XsdFxOptionTradeMapper;
import org.example.fidstp2.mapper.XsdTradeEnvelopeReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class XsdTradeMessageParser implements TradeMessageParser<FxOptionTrade> {
    private final XsdTradeEnvelopeReader reader;
    private final XsdFxOptionTradeMapper mapper;

    public XsdTradeMessageParser() {
        this(new XsdTradeEnvelopeReader(), new XsdFxOptionTradeMapper());
    }

    public XsdTradeMessageParser(XsdTradeEnvelopeReader reader, XsdFxOptionTradeMapper mapper) {
        this.reader = Objects.requireNonNull(reader, "reader is required");
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
    }

    @Override
    public FxOptionTrade parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new TradeParsingException("raw trade payload is required");
        }
        try {
            ByteArrayInputStream xml = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
            return mapper.toDomain(reader.read(xml));
        } catch (IllegalArgumentException ex) {
            throw new TradeParsingException("failed to parse trade xml", ex);
        }
    }
}

