package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ForbiddenExceptionTest {

    @Test
    void defaultConstructor_usesDefaultMessageAndCorrectStatus() {
        ForbiddenException ex = new ForbiddenException();

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(ex.getMessage()).isEqualTo(ErrorMessage.FORBIDDEN);
    }

    @Test
    void customMessageConstructor_overridesMessage_keepsStatusAndErrorCode() {
        ForbiddenException ex = new ForbiddenException("Bạn không phải chủ sở hữu Deck này");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(ex.getMessage()).isEqualTo("Bạn không phải chủ sở hữu Deck này");
    }
}
