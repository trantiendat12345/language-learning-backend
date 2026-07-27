package com.languagelearning.language_learning_backend.auth.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void serialize_neverIncludesRefreshTokenField() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token-value")
                .refreshToken("raw-refresh-token-should-never-appear-in-json")
                .build();

        String json = new ObjectMapper().writeValueAsString(response);

        assertThat(json).contains("access-token-value").doesNotContain("raw-refresh-token-should-never-appear-in-json");
    }
}
