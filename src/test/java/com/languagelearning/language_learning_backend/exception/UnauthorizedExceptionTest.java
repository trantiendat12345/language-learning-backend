package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class UnauthorizedExceptionTest {

    @Test
    void defaultConstructor_usesDefaultMessageAndCorrectStatus() {
        UnauthorizedException ex = new UnauthorizedException();

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        assertThat(ex.getMessage()).isEqualTo(ErrorMessage.UNAUTHORIZED);
    }

    @Test
    void customMessageConstructor_overridesMessage_keepsStatusAndErrorCode() {
        UnauthorizedException ex = new UnauthorizedException("Token đã hết hạn");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        assertThat(ex.getMessage()).isEqualTo("Token đã hết hạn");
    }
}
