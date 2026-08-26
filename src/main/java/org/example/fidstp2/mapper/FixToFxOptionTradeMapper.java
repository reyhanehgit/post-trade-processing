package org.example.fidstp2.mapper;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.dto.FixMessageDto;
import org.example.fidstp2.exception.TradeParsingException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FixToFxOptionTradeMapper {
    private static final DateTimeFormatter FIX_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public FxOptionTrade map(FixMessageDto fixMessage) {
        try {
            LocalDate tradeDate = parseDate(required(fixMessage, "75", "tradeDate"), "tradeDate");
            String valueDateRaw = firstPresent(fixMessage, List.of("64", "20008"));
            LocalDate valueDate = valueDateRaw == null ? tradeDate : parseDate(valueDateRaw, "valueDate");

            String strike = firstPresent(fixMessage, List.of("44", "20002"));
            if (strike == null || strike.isBlank()) {
                throw new TradeParsingException("missing FIX tag for strikePrice (44 or 20002)");
            }

            return new FxOptionTrade(
                    required(fixMessage, "11", "tradeId"),
                    optionalWithFallback(fixMessage, List.of("37", "17", "11")),
                    normalizeProductType(firstPresent(fixMessage, List.of("20000"))),
                    required(fixMessage, "55", "currencyPair").toUpperCase(Locale.ROOT),
                    parseDecimal(required(fixMessage, "38", "notionalAmount"), "notionalAmount"),
                    required(fixMessage, "15", "notionalCurrency").toUpperCase(Locale.ROOT),
                    parseSide(required(fixMessage, "54", "buySell")),
                    tradeDate,
                    valueDate,
                    requiredWithFallback(fixMessage, List.of("1", "448", "20005"), "counterpartyId"),
                    requiredWithFallback(fixMessage, List.of("20006", "128"), "legalEntityId"),
                    required(fixMessage, "49", "sourceSystem"),
                    ProcessingStatus.RECEIVED,
                    Instant.now(),
                    null,
                    parseOptionType(required(fixMessage, "20001", "optionType")),
                    parseDecimal(strike, "strikePrice"),
                    parseDate(required(fixMessage, "20003", "expiryDate"), "expiryDate"),
                    parseOptionStyle(required(fixMessage, "20004", "optionStyle")),
                    List.of()
            );
        } catch (IllegalArgumentException ex) {
            throw new TradeParsingException("FIX message could not be mapped to FxOptionTrade: " + ex.getMessage(), ex);
        }
    }

    private static String required(FixMessageDto message, String key, String fieldName) {
        String value = message.get(key);
        if (value == null || value.isBlank()) {
            throw new TradeParsingException("missing FIX tag " + key + " for " + fieldName);
        }
        return value;
    }

    private static String requiredWithFallback(FixMessageDto message, List<String> keys, String fieldName) {
        String value = optionalWithFallback(message, keys);
        if (value == null || value.isBlank()) {
            throw new TradeParsingException("missing FIX tag for " + fieldName + " (checked " + String.join(",", keys) + ")");
        }
        return value;
    }

    private static String optionalWithFallback(FixMessageDto message, List<String> keys) {
        String value = firstPresent(message, keys);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private static String firstPresent(FixMessageDto message, List<String> keys) {
        for (String key : keys) {
            String value = message.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value, FIX_DATE);
        } catch (Exception ex) {
            throw new TradeParsingException("invalid date format for " + fieldName + ": expected yyyyMMdd");
        }
    }

    private static BigDecimal parseDecimal(String value, String fieldName) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new TradeParsingException("invalid decimal for " + fieldName + ": " + value);
        }
    }

    private static BuySell parseSide(String value) {
        return switch (value) {
            case "1", "BUY" -> BuySell.BUY;
            case "2", "SELL" -> BuySell.SELL;
            default -> throw new TradeParsingException("unsupported side tag 54 value: " + value);
        };
    }

    private static OptionType parseOptionType(String value) {
        try {
            return OptionType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new TradeParsingException("invalid optionType: " + value);
        }
    }

    private static OptionStyle parseOptionStyle(String value) {
        try {
            return OptionStyle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new TradeParsingException("invalid optionStyle: " + value);
        }
    }

    private static String normalizeProductType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "FX_OPTION";
        }
        String value = raw.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

