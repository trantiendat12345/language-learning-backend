package com.languagelearning.language_learning_backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

    private static final Pattern SAFE_URL_PATTERN = Pattern.compile("^(https?://.+|/.*)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return SAFE_URL_PATTERN.matcher(value).matches();
    }
}
