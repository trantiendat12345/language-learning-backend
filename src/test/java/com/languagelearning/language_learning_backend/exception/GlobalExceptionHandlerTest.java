package com.languagelearning.language_learning_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiErrorResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException_mapsHttpStatusAndErrorCodeFromException() {
        BusinessException ex = new ResourceNotFoundException();

        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorMessage.RESOURCE_NOT_FOUND);
    }

    @Test
    void handleValidationException_collectsAllFieldErrorsInto400Response() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("registerRequest", "email", "Email không đúng định dạng");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("Email không đúng định dạng");
    }

    @Test
    void handleHttpMessageNotReadableException_returns400_notSwallowedByGenericHandler() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadableException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorMessage.BAD_REQUEST);
    }

    @Test
    void handleNoResourceFoundException_returns404_notSwallowedByGenericHandler() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/khong-ton-tai");

        ResponseEntity<ApiErrorResponse> response = handler.handleNoResourceFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void handleUnexpectedException_returns500WithGenericMessage_doesNotLeakRealErrorDetail() {
        Exception ex = new RuntimeException("Connection refused: jdbc:mysql://internal-db-host:3306");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorMessage.INTERNAL_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("jdbc:mysql", "internal-db-host");
    }
}
