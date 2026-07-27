package com.languagelearning.language_learning_backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
import com.languagelearning.language_learning_backend.auth.dto.response.AuthResponse;
import com.languagelearning.language_learning_backend.auth.exception.AccountDisabledException;
import com.languagelearning.language_learning_backend.auth.exception.AccountLockedException;
import com.languagelearning.language_learning_backend.auth.exception.EmailNotVerifiedException;
import com.languagelearning.language_learning_backend.auth.exception.EmailTakenException;
import com.languagelearning.language_learning_backend.auth.exception.InvalidCredentialsException;
import com.languagelearning.language_learning_backend.auth.exception.PasswordMismatchException;
import com.languagelearning.language_learning_backend.auth.exception.UsernameTakenException;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.auth.repository.VerificationTokenRepository;
import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.role.repository.RoleRepository;
import com.languagelearning.language_learning_backend.security.JwtService;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleRepository,
                verificationTokenRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
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
        verify(verificationTokenRepository).save(any());
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
}
