package org.example.fidstp2.parser;

import org.example.fidstp2.dto.FixMessageDto;

public interface FixMessageAdapter {
    FixMessageDto adapt(String rawMessage);
}

