package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DuplicateResourceExceptionTest {

    @Test
    void defaultConstructor_usesDefaultMessageAndCorrectStatus() {
        DuplicateResourceException ex = new DuplicateResourceException();

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
        assertThat(ex.getMessage()).isEqualTo(ErrorMessage.DUPLICATE_RESOURCE);
    }

    @Test
    void customMessageConstructor_overridesMessage_keepsStatusAndErrorCode() {
        DuplicateResourceException ex = new DuplicateResourceException("Username đã tồn tại");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
        assertThat(ex.getMessage()).isEqualTo("Username đã tồn tại");
    }
}
