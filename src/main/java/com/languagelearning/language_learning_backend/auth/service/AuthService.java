package com.languagelearning.language_learning_backend.auth.service;

import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
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
import com.languagelearning.language_learning_backend.auth.exception.UsernameTakenException;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.auth.repository.VerificationTokenRepository;
import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.role.repository.RoleRepository;
import com.languagelearning.language_learning_backend.security.JwtService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nghiệp vụ đăng ký/đăng nhập. Login kiểm tra `User.status` thủ công (không dùng
 * Spring AuthenticationManager) để ném đúng exception con theo từng trạng thái tài khoản
 * (DISABLED/LOCKED/PENDING_VERIFICATION cần message riêng biệt — xem
 * docs/testing/11_FRS_TC_AUTH.md mục 1.2).
 */
@Slf4j
@Service
public class AuthService {

    private static final String DEFAULT_ROLE_CODE = "USER";
    private static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";
    private static final int EMAIL_VERIFY_TOKEN_HOURS = 24;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Tạo tài khoản mới với status PENDING_VERIFICATION và VerificationToken type EMAIL_VERIFY.
     * MVP: link xác thực chỉ log ra console thay vì gửi email thật (xem 11_FRS_TC_AUTH.md mục 1.1).
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameTakenException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailTakenException();
        }

        Role userRole = roleRepository
                .findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Role mặc định '" + DEFAULT_ROLE_CODE + "' chưa được seed - kiểm tra RoleSeeder"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTimezone(DEFAULT_TIMEZONE);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        String rawToken = generateOpaqueToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setType(TokenType.EMAIL_VERIFY);
        verificationToken.setTokenHash(rawToken);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(EMAIL_VERIFY_TOKEN_HOURS));
        verificationTokenRepository.save(verificationToken);

        log.info(
                "[MVP email] Link xác thực cho {}: GET /api/auth/verify-email?token={}",
                user.getEmail(),
                rawToken);

        return toUserResponse(user);
    }

    /**
     * Xác thực username/email + password, kiểm tra status tài khoản, rồi cấp Access Token
     * (JWT) + Refresh Token (chuỗi ngẫu nhiên lưu DB để có thể revoke).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        switch (user.getStatus()) {
            case PENDING_VERIFICATION -> throw new EmailNotVerifiedException();
            case DISABLED -> throw new AccountDisabledException();
            case LOCKED -> throw new AccountLockedException();
            case ACTIVE -> {
                // hợp lệ, tiếp tục cấp token
            }
        }

        List<String> roleCodes = user.getRoles().stream().map(Role::getCode).toList();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), roleCodes);

        String rawRefreshToken = generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(rawRefreshToken);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder().accessToken(accessToken).refreshToken(rawRefreshToken).build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .build();
    }

    /** Sinh chuỗi ngẫu nhiên dùng làm token cho VerificationToken/RefreshToken (không phải JWT). */
    private String generateOpaqueToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
