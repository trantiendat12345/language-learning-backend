package com.languagelearning.language_learning_backend.auth.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegisterRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser01");
        request.setEmail("newuser01@test.com");
        request.setPassword("Passw0rd1");
        request.setConfirmPassword("Passw0rd1");
        return request;
    }

    @Test
    void validate_withValidData_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void validate_whenUsernameContainsWhitespace_rejectsUsername() {
        RegisterRequest request = validRequest();
        request.setUsername("new user01");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("username");
    }

    @Test
    void validate_whenPasswordHasNoDigit_rejectsPassword() {
        RegisterRequest request = validRequest();
        request.setPassword("PasswordOnly");
        request.setConfirmPassword("PasswordOnly");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    @Test
    void validate_whenEmailInvalid_rejectsEmail() {
        RegisterRequest request = validRequest();
        request.setEmail("abc123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("email");
    }
}
