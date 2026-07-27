package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BadRequestExceptionTest {

    @Test
    void defaultConstructor_usesDefaultMessageAndCorrectStatus() {
        BadRequestException ex = new BadRequestException();

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo(ErrorMessage.BAD_REQUEST);
    }

    @Test
    void customMessageConstructor_overridesMessage_keepsStatusAndErrorCode() {
        BadRequestException ex = new BadRequestException("Ngày sinh không hợp lệ");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("Ngày sinh không hợp lệ");
    }
}
