package com.languagelearning.language_learning_backend.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SafeUrlValidatorTest {

    private final SafeUrlValidator validator = new SafeUrlValidator();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"https://cdn.example.com/a.png", "http://cdn.example.com/a.png", "/uploads/a.png"})
    void isValid_whenNullOrHttpOrRelative_returnsTrue(String value) {
        assertThat(validator.isValid(value, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "data:text/html,<script>alert(1)</script>", "vbscript:msgbox(1)", "file:///etc/passwd"})
    void isValid_whenDangerousScheme_returnsFalse(String value) {
        assertThat(validator.isValid(value, null)).isFalse();
    }
}
