package org.example.fidstp2.validator;

import java.util.Locale;

public final class ProductTypeNormalizer {
    private ProductTypeNormalizer() {
    }

    public static String normalize(String rawProductType) {
        if (rawProductType == null || rawProductType.isBlank()) {
            return "FX_OPTION";
        }
        String value = rawProductType.toUpperCase(Locale.ROOT);
        return "OPTION".equals(value) ? "FX_OPTION" : value;
    }
}

