package org.example.fidstp2.validator;

import org.example.fidstp2.domain.FxOptionTrade;
import org.example.fidstp2.domain.OptionStyle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SpotTradeValidator implements ProductTypeTradeValidator {
    private static final Pattern CURRENCY_PAIR_PATTERN = Pattern.compile("^[A-Z]{3}/[A-Z]{3}$");
    private static final Pattern ISO_CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final String STAGE = "VALIDATION";

    @Override
    public boolean supports(String productType) {
        return "SPOT".equals(productType);
    }

    @Override
    public ValidationResult validate(FxOptionTrade trade) {
        List<ValidationError> errors = new ArrayList<>();
        if (trade == null) {
            errors.add(error(null, "TRADE_NULL", "trade payload is required"));
            return ValidationResult.invalid(errors);
        }

        String tradeId = trade.getTradeId();

        String productType = trade.getProductType() == null ? "" : trade.getProductType().toUpperCase(Locale.ROOT);
        if (!"SPOT".equals(productType)) {
            errors.add(error(tradeId, "PRODUCT_TYPE_INVALID", "productType must be SPOT"));
        }

        if (!CURRENCY_PAIR_PATTERN.matcher(trade.getCurrencyPair()).matches()) {
            errors.add(error(tradeId, "CURRENCY_PAIR_INVALID", "currencyPair must match AAA/BBB"));
        }

        if (!ISO_CURRENCY_PATTERN.matcher(trade.getNotionalCurrency().toUpperCase(Locale.ROOT)).matches()) {
            errors.add(error(tradeId, "NOTIONAL_CCY_INVALID", "notionalCurrency must be a 3-letter ISO code"));
        }

        if (trade.getNotionalAmount().signum() <= 0) {
            errors.add(error(tradeId, "NOTIONAL_INVALID", "notionalAmount must be positive"));
        }

        if (trade.getStrikePrice().signum() <= 0) {
            errors.add(error(tradeId, "STRIKE_INVALID", "strikePrice must be positive"));
        }

        if (trade.getExpiryDate().isBefore(trade.getTradeDate())) {
            errors.add(error(tradeId, "EXPIRY_INVALID", "expiryDate must be on or after tradeDate"));
        }

        if (trade.getOptionStyle() == OptionStyle.MULTI_LEG && trade.getLegs().isEmpty()) {
            errors.add(error(tradeId, "LEGS_MISSING", "multi-leg option must include at least one leg"));
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private ValidationError error(String tradeId, String code, String message) {
        return new ValidationError(tradeId, STAGE, code, message, Instant.now());
    }
}

