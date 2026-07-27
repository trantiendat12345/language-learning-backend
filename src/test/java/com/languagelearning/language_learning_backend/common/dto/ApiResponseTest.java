package com.languagelearning.language_learning_backend.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void success_withDataOnly_usesDefaultSuccessMessage() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo(CommonMessage.SUCCESS);
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void success_withCustomMessage_keepsGivenMessage() {
        ApiResponse<Integer> response = ApiResponse.success("Đăng ký thành công", 42);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Đăng ký thành công");
        assertThat(response.getData()).isEqualTo(42);
    }
}
