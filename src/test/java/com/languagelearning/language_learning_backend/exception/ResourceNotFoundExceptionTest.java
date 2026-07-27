package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ResourceNotFoundExceptionTest {

    @Test
    void defaultConstructor_usesDefaultMessageAndCorrectStatus() {
        ResourceNotFoundException ex = new ResourceNotFoundException();

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo(ErrorMessage.RESOURCE_NOT_FOUND);
    }

    @Test
    void customMessageConstructor_overridesMessage_keepsStatusAndErrorCode() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Không tìm thấy Deck");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Không tìm thấy Deck");
    }
}
