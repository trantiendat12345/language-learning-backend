package com.languagelearning.language_learning_backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.auth.dto.request.ForgotPasswordRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.ResetPasswordRequest;
import com.languagelearning.language_learning_backend.auth.dto.response.AccessTokenResponse;
import com.languagelearning.language_learning_backend.auth.dto.response.AuthResponse;
import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import com.languagelearning.language_learning_backend.auth.entity.VerificationToken;
import com.languagelearning.language_learning_backend.auth.enums.TokenType;
import com.languagelearning.language_learning_backend.auth.exception.AccountDisabledException;
import com.languagelearning.language_learning_backend.auth.exception.AccountLockedException;
import com.languagelearning.language_learning_backend.auth.exception.EmailNotVerifiedException;
import com.languagelearning.language_learning_backend.auth.exception.EmailTakenException;
import com.languagelearning.language_learning_backend.auth.exception.InvalidCredentialsException;
import com.languagelearning.language_learning_backend.auth.exception.PasswordMismatchException;
import com.languagelearning.language_learning_backend.auth.exception.TokenAlreadyUsedException;
import com.languagelearning.language_learning_backend.auth.exception.TokenExpiredException;
import com.languagelearning.language_learning_backend.auth.exception.TokenInvalidException;
import com.languagelearning.language_learning_backend.auth.exception.UsernameTakenException;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.auth.repository.VerificationTokenRepository;
import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.role.repository.RoleRepository;
import com.languagelearning.language_learning_backend.security.JwtService;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.mapstruct.factory.Mappers;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<VerificationToken> verificationTokenCaptor;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // UserMapper chỉ map field 1-1, không có logic cần mock - dùng bản implementation
        // thật do MapStruct generate thay vì Mockito mock (mock sẽ luôn trả null, làm sai
        // các assertion đọc field từ UserResponse trả về).
        authService = new AuthService(
                userRepository,
                roleRepository,
                verificationTokenRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                Mappers.getMapper(UserMapper.class),
                604_800_000L);
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser01");
        request.setEmail("newuser01@test.com");
        request.setPassword("Passw0rd1");
        request.setConfirmPassword("Passw0rd1");
        return request;
    }

    @Test
    void register_withValidData_createsUserWithPendingVerificationStatus() {
        RegisterRequest request = validRegisterRequest();
        Role userRole = new Role();
        userRole.setCode("USER");
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(userRepository.existsByEmail("newuser01@test.com")).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Passw0rd1")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("newuser01");
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        verify(verificationTokenRepository).save(verificationTokenCaptor.capture());
        // Không thể lấy trực tiếp token gốc (chỉ log ra, không trả về UserResponse), nhưng
        // vẫn khẳng định được: giá trị lưu DB đúng định dạng SHA-256 hex digest (64 ký tự
        // hex thường) — bằng chứng gián tiếp hashToken() đã chạy. Test chính xác hơn (so
        // token gốc vs hash) nằm ở login_storesRefreshTokenAsSha256Hash_notThePlaintextValue
        // bên dưới, vì login trả token gốc ra AuthResponse nên so sánh được trực tiếp.
        assertThat(verificationTokenCaptor.getValue().getTokenHash()).hasSize(64).matches("^[0-9a-f]{64}$");
    }

    @Test
    void register_whenPasswordAndConfirmPasswordMismatch_throwsPasswordMismatchException() {
        RegisterRequest request = validRegisterRequest();
        request.setConfirmPassword("Different1");

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenUsernameAlreadyTaken_throwsUsernameTakenException() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByUsername("newuser01")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(UsernameTakenException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenEmailAlreadyTaken_throwsEmailTakenException() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(userRepository.existsByEmail("newuser01@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(EmailTakenException.class);
        verify(userRepository, never()).save(any());
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user01");
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of());
        return user;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("user01");
        request.setPassword("Passw0rd1");
        return request;
    }

    @Test
    void login_withCorrectCredentialsAndActiveStatus_returnsTokens() {
        User user = activeUser();
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rd1", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, "user01", java.util.List.of())).thenReturn("access-token");

        AuthResponse response = authService.login(loginRequest());

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_storesRefreshTokenAsSha256Hash_notThePlaintextValue() throws Exception {
        User user = activeUser();
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rd1", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, "user01", java.util.List.of())).thenReturn("access-token");

        AuthResponse response = authService.login(loginRequest());

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        String rawTokenReturnedToClient = response.getRefreshToken();
        String storedHash = refreshTokenCaptor.getValue().getTokenHash();

        assertThat(storedHash).isNotEqualTo(rawTokenReturnedToClient).isEqualTo(sha256Hex(rawTokenReturnedToClient));
    }

    @Test
    void login_whenUserNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_whenPasswordWrong_throwsInvalidCredentialsException() {
        User user = activeUser();
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_whenStatusPendingVerification_throwsEmailNotVerifiedException() {
        User user = activeUser();
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void login_whenStatusDisabled_throwsAccountDisabledException() {
        User user = activeUser();
        user.setStatus(UserStatus.DISABLED);
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(AccountDisabledException.class);
    }

    @Test
    void login_whenStatusLocked_throwsAccountLockedException() {
        User user = activeUser();
        user.setStatus(UserStatus.LOCKED);
        when(userRepository.findByUsernameOrEmail("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(AccountLockedException.class);
    }

    private RefreshToken activeRefreshToken(User user, String rawToken) {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setUser(user);
        token.setTokenHash(sha256Hex(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(false);
        return token;
    }

    @Test
    void refreshAccessToken_withValidToken_returnsNewAccessToken() {
        User user = activeUser();
        RefreshToken token = activeRefreshToken(user, "raw-refresh-token");
        when(refreshTokenRepository.findByTokenHash(sha256Hex("raw-refresh-token"))).thenReturn(Optional.of(token));
        when(jwtService.generateAccessToken(1L, "user01", List.of())).thenReturn("new-access-token");

        AccessTokenResponse response = authService.refreshAccessToken("raw-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refreshAccessToken_whenTokenNotFound_throwsTokenInvalidException() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken("unknown-token"))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void refreshAccessToken_whenCookieMissing_throwsTokenInvalidExceptionNotNullPointerException() {
        assertThatThrownBy(() -> authService.refreshAccessToken(null)).isInstanceOf(TokenInvalidException.class);
        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void refreshAccessToken_whenTokenRevoked_throwsTokenInvalidException() {
        User user = activeUser();
        RefreshToken token = activeRefreshToken(user, "raw-refresh-token");
        token.setRevoked(true);
        when(refreshTokenRepository.findByTokenHash(sha256Hex("raw-refresh-token"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refreshAccessToken("raw-refresh-token"))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void refreshAccessToken_whenTokenExpired_throwsTokenExpiredException() {
        User user = activeUser();
        RefreshToken token = activeRefreshToken(user, "raw-refresh-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(sha256Hex("raw-refresh-token"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refreshAccessToken("raw-refresh-token"))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void logout_withTokenOwnedByCurrentUser_revokesToken() {
        User user = activeUser();
        RefreshToken token = activeRefreshToken(user, "raw-refresh-token");
        when(refreshTokenRepository.findByTokenHash(sha256Hex("raw-refresh-token"))).thenReturn(Optional.of(token));

        authService.logout(1L, "raw-refresh-token");

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        assertThat(refreshTokenCaptor.getValue().isRevoked()).isTrue();
    }

    @Test
    void logout_withNullCookie_doesNothing() {
        authService.logout(1L, null);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_whenTokenBelongsToDifferentUser_doesNotRevoke() {
        User user = activeUser();
        RefreshToken token = activeRefreshToken(user, "raw-refresh-token");
        when(refreshTokenRepository.findByTokenHash(sha256Hex("raw-refresh-token"))).thenReturn(Optional.of(token));

        authService.logout(999L, "raw-refresh-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    private ForgotPasswordRequest forgotPasswordRequest() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user01@test.com");
        return request;
    }

    @Test
    void forgotPassword_whenEmailExists_createsPasswordResetToken() {
        User user = activeUser();
        when(userRepository.findByEmail("user01@test.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(forgotPasswordRequest());

        verify(verificationTokenRepository).save(verificationTokenCaptor.capture());
        assertThat(verificationTokenCaptor.getValue().getType()).isEqualTo(TokenType.PASSWORD_RESET);
    }

    @Test
    void forgotPassword_whenEmailDoesNotExist_doesNotCreateToken() {
        when(userRepository.findByEmail("user01@test.com")).thenReturn(Optional.empty());

        authService.forgotPassword(forgotPasswordRequest());

        verify(verificationTokenRepository, never()).save(any());
    }

    private VerificationToken activePasswordResetToken(User user, String rawToken) {
        VerificationToken token = new VerificationToken();
        token.setId(1L);
        token.setUser(user);
        token.setType(TokenType.PASSWORD_RESET);
        token.setTokenHash(sha256Hex(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        return token;
    }

    private ResetPasswordRequest resetPasswordRequest(String token) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("NewPassw0rd1");
        request.setConfirmNewPassword("NewPassw0rd1");
        return request;
    }

    @Test
    void resetPassword_withValidToken_updatesPasswordAndRevokesOldRefreshTokens() {
        User user = activeUser();
        VerificationToken token = activePasswordResetToken(user, "raw-reset-token");
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-reset-token"), TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassw0rd1")).thenReturn("new-hashed-password");
        RefreshToken oldToken = activeRefreshToken(user, "old-refresh-token");
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L)).thenReturn(List.of(oldToken));

        authService.resetPassword(resetPasswordRequest("raw-reset-token"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(oldToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAll(List.of(oldToken));
    }

    @Test
    void resetPassword_whenPasswordMismatch_throwsPasswordMismatchException() {
        ResetPasswordRequest request = resetPasswordRequest("raw-reset-token");
        request.setConfirmNewPassword("Different1");

        assertThatThrownBy(() -> authService.resetPassword(request)).isInstanceOf(PasswordMismatchException.class);
        verify(verificationTokenRepository, never()).findByTokenHashAndType(anyString(), any());
    }

    @Test
    void resetPassword_whenTokenNotFound_throwsTokenInvalidException() {
        when(verificationTokenRepository.findByTokenHashAndType(anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequest("unknown-token")))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void resetPassword_whenTokenAlreadyUsed_throwsTokenAlreadyUsedException() {
        User user = activeUser();
        VerificationToken token = activePasswordResetToken(user, "raw-reset-token");
        token.setUsedAt(LocalDateTime.now().minusMinutes(5));
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-reset-token"), TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequest("raw-reset-token")))
                .isInstanceOf(TokenAlreadyUsedException.class);
    }

    @Test
    void resetPassword_whenTokenExpired_throwsTokenExpiredException() {
        User user = activeUser();
        VerificationToken token = activePasswordResetToken(user, "raw-reset-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-reset-token"), TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequest("raw-reset-token")))
                .isInstanceOf(TokenExpiredException.class);
    }

    private VerificationToken activeEmailVerifyToken(User user, String rawToken) {
        VerificationToken token = new VerificationToken();
        token.setId(1L);
        token.setUser(user);
        token.setType(TokenType.EMAIL_VERIFY);
        token.setTokenHash(sha256Hex(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return token;
    }

    @Test
    void verifyEmail_withValidToken_activatesUser() {
        User user = activeUser();
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        VerificationToken token = activeEmailVerifyToken(user, "raw-verify-token");
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-verify-token"), TokenType.EMAIL_VERIFY))
                .thenReturn(Optional.of(token));

        authService.verifyEmail("raw-verify-token");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void verifyEmail_whenTokenNotFound_throwsTokenInvalidException() {
        when(verificationTokenRepository.findByTokenHashAndType(anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("unknown-token")).isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void verifyEmail_whenTokenNull_throwsTokenInvalidExceptionNotNullPointerException() {
        assertThatThrownBy(() -> authService.verifyEmail(null)).isInstanceOf(TokenInvalidException.class);
        verify(verificationTokenRepository, never()).findByTokenHashAndType(any(), any());
    }

    @Test
    void verifyEmail_whenTokenAlreadyUsed_throwsTokenAlreadyUsedException() {
        User user = activeUser();
        VerificationToken token = activeEmailVerifyToken(user, "raw-verify-token");
        token.setUsedAt(LocalDateTime.now().minusHours(1));
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-verify-token"), TokenType.EMAIL_VERIFY))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail("raw-verify-token")).isInstanceOf(TokenAlreadyUsedException.class);
    }

    @Test
    void verifyEmail_whenTokenExpired_throwsTokenExpiredException() {
        User user = activeUser();
        VerificationToken token = activeEmailVerifyToken(user, "raw-verify-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(verificationTokenRepository.findByTokenHashAndType(sha256Hex("raw-verify-token"), TokenType.EMAIL_VERIFY))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail("raw-verify-token")).isInstanceOf(TokenExpiredException.class);
    }

    /** Tính SHA-256 hex y hệt AuthService.hashToken() để mock/assert theo giá trị đã hash. */
    private static String sha256Hex(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
