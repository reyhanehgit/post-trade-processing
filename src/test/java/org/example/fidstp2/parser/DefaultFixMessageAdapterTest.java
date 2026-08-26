package org.example.fidstp2.parser;

import org.example.fidstp2.dto.FixMessageDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultFixMessageAdapterTest {

    private final DefaultFixMessageAdapter adapter = new DefaultFixMessageAdapter();

    @Test
    void adaptsPipeDelimitedFixToTags() {
        FixMessageDto dto = adapter.adapt("11=T-1|55=EUR/USD|54=1|");

        assertEquals("T-1", dto.get("11"));
        assertEquals("EUR/USD", dto.get("55"));
        assertEquals("1", dto.get("54"));
    }

    @Test
    void adaptsSohDelimitedFixToTags() {
        FixMessageDto dto = adapter.adapt("11=T-2\u000155=GBP/USD\u000154=2\u0001");

        assertEquals("T-2", dto.get("11"));
        assertEquals("GBP/USD", dto.get("55"));
        assertEquals("2", dto.get("54"));
    }
}

