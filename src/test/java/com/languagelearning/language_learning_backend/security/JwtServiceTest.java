package com.languagelearning.language_learning_backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    // Secret chỉ dùng cho test, không phải secret thật của môi trường nào - đủ dài cho HS512 (>= 64 byte).
    private static final String TEST_SECRET =
            "Xo1Icq0QWhDP+ZEolqbBQGpVhmypYAk7M6kRS7uHHH+/UEFn+Cw8gnWCgx4+JYRB2Db99AyvHHnowO9YBsNxPw==";
    private static final String OTHER_SECRET =
            "1b1grBY9pX2x5kxpTLQJvtGvl/nsFRKXPfIwA5aGENXt4TzthmuKNWKxDxGHVYbOI/5CZFRRqvotxKefmSQsRg==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 60_000);
    }

    @Test
    void generateAccessToken_thenParse_roundTripsUserIdAndUsername() {
        String token = jwtService.generateAccessToken(42L, "user01", List.of("USER"));

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("user01");
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-real-token")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() throws InterruptedException {
        JwtService shortLivedService = new JwtService(TEST_SECRET, 1);
        String token = shortLivedService.generateAccessToken(1L, "user01", List.of("USER"));
        Thread.sleep(20);

        assertThat(shortLivedService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseWhenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService(OTHER_SECRET, 60_000);
        String token = otherService.generateAccessToken(1L, "user01", List.of("USER"));

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
