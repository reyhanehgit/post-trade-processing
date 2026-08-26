package org.example.fidstp2.validator;

import java.util.ArrayList;
import java.util.List;

public final class ValidationResult {
    private static final ValidationResult VALID = new ValidationResult(true, List.of());

    private final boolean valid;
    private final List<ValidationError> errors;

    private ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = List.copyOf(new ArrayList<>(errors));
    }

    public static ValidationResult valid() {
        return VALID;
    }

    public static ValidationResult invalid(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("errors are required for invalid results");
        }
        return new ValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}

