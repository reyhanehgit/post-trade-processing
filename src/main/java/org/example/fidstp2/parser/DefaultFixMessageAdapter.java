package org.example.fidstp2.parser;

import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.exception.TradeParsingException;

import java.util.HashMap;
import java.util.Map;

public class DefaultFixMessageAdapter implements FixMessageAdapter {
    @Override
    public FixMessageDto adapt(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new TradeParsingException("raw FIX message is required");
        }

        String normalized = rawMessage.contains("\u0001") ? rawMessage.replace('\u0001', '|') : rawMessage;
        String[] fields = normalized.split("\\|");
        Map<String, String> tags = new HashMap<>();

        for (String field : fields) {
            if (field.isBlank()) {
                continue;
            }
            int sepIndex = field.indexOf('=');
            if (sepIndex <= 0 || sepIndex == field.length() - 1) {
                continue;
            }
            tags.put(field.substring(0, sepIndex).trim(), field.substring(sepIndex + 1).trim());
        }

        return new FixMessageDto(tags);
    }
}

