package org.example.fidstp2.exception;

public class TradeParsingException extends RuntimeException {
    public TradeParsingException(String message) {
        super(message);
    }

    public TradeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

