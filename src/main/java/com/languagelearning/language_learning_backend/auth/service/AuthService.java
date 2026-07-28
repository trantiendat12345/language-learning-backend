package com.languagelearning.language_learning_backend.auth.service;

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
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
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
    private static final int PASSWORD_RESET_TOKEN_MINUTES = 20;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
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

        String rawToken = createVerificationToken(user, TokenType.EMAIL_VERIFY, Duration.ofHours(EMAIL_VERIFY_TOKEN_HOURS));
        log.info(
                "[MVP email] Link xác thực cho {}: GET /api/auth/verify-email?token={}",
                user.getEmail(),
                rawToken);

        return userMapper.toResponse(user);
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
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder().accessToken(accessToken).refreshToken(rawRefreshToken).build();
    }

    /**
     * Cấp Access Token mới từ Refresh Token còn hạn, chưa bị revoke. KHÔNG rotate Refresh
     * Token (giữ nguyên tới khi hết hạn hoặc logout) — đúng phạm vi mô tả ở
     * docs/testing/11_FRS_TC_AUTH.md mục 1.4.
     */
    @Transactional(readOnly = true)
    public AccessTokenResponse refreshAccessToken(String rawRefreshToken) {
        RefreshToken refreshToken = validateRefreshToken(rawRefreshToken);
        User user = refreshToken.getUser();

        List<String> roleCodes = user.getRoles().stream().map(Role::getCode).toList();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), roleCodes);

        return AccessTokenResponse.builder().accessToken(accessToken).build();
    }

    /**
     * Revoke Refresh Token hiện tại (đăng xuất). Kiểm tra token thuộc đúng user đang gọi API
     * (ownership check bắt buộc — currentUserId lấy từ SecurityContext, xem CLAUDE.md #6)
     * trước khi revoke; không ném lỗi nếu cookie thiếu/không hợp lệ — logout luôn thành công
     * phía client dù server không tìm thấy gì để revoke.
     */
    @Transactional
    public void logout(Long currentUserId, String rawRefreshToken) {
        if (rawRefreshToken == null) {
            return;
        }
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken));
        if (tokenOpt.isEmpty()) {
            return;
        }
        RefreshToken refreshToken = tokenOpt.get();
        if (!refreshToken.getUser().getId().equals(currentUserId)) {
            log.warn(
                    "Logout: refreshToken cookie không thuộc userId={} đang gọi API - bỏ qua revoke",
                    currentUserId);
            return;
        }
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Tạo VerificationToken type PASSWORD_RESET nếu email tồn tại. Luôn hành xử giống nhau
     * (Controller trả cùng message) dù email có tồn tại hay không, để không lộ email nào đã
     * đăng ký — xem docs/testing/11_FRS_TC_AUTH.md mục 1.5.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String rawToken =
                    createVerificationToken(user, TokenType.PASSWORD_RESET, Duration.ofMinutes(PASSWORD_RESET_TOKEN_MINUTES));
            log.info(
                    "[MVP email] Link đặt lại mật khẩu cho {}: POST /api/auth/reset-password (token={})",
                    user.getEmail(),
                    rawToken);
        });
    }

    /**
     * Đặt lại mật khẩu bằng token PASSWORD_RESET hợp lệ. Sau khi đổi thành công, revoke toàn
     * bộ Refresh Token cũ của user (bảo mật — buộc đăng nhập lại trên mọi thiết bị, xem
     * docs/testing/11_FRS_TC_AUTH.md mục 1.5).
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException();
        }

        VerificationToken token = validateVerificationToken(request.getToken(), TokenType.PASSWORD_RESET);
        User user = token.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(token);

        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(user.getId());
        activeTokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }

    /**
     * Xác thực email bằng token EMAIL_VERIFY hợp lệ — chuyển User.status từ
     * PENDING_VERIFICATION sang ACTIVE.
     */
    @Transactional
    public void verifyEmail(String rawToken) {
        VerificationToken token = validateVerificationToken(rawToken, TokenType.EMAIL_VERIFY);
        User user = token.getUser();

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(token);
    }

    /** Sinh chuỗi ngẫu nhiên dùng làm token cho VerificationToken/RefreshToken (không phải JWT). */
    private String generateOpaqueToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    /** Tạo + lưu 1 VerificationToken mới (đã hash), trả token gốc để caller tự log/gửi. */
    private String createVerificationToken(User user, TokenType type, Duration validity) {
        String rawToken = generateOpaqueToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setType(type);
        verificationToken.setTokenHash(hashToken(rawToken));
        verificationToken.setExpiresAt(LocalDateTime.now().plus(validity));
        verificationTokenRepository.save(verificationToken);
        return rawToken;
    }

    /**
     * Tra + kiểm tra hợp lệ 1 VerificationToken theo token gốc nhận từ client (reset-password/
     * verify-email dùng chung logic này) — không tồn tại/sai type → TokenInvalidException, đã
     * dùng rồi → TokenAlreadyUsedException, hết hạn → TokenExpiredException.
     */
    private VerificationToken validateVerificationToken(String rawToken, TokenType type) {
        if (rawToken == null) {
            throw new TokenInvalidException();
        }
        VerificationToken token = verificationTokenRepository
                .findByTokenHashAndType(hashToken(rawToken), type)
                .orElseThrow(TokenInvalidException::new);
        if (token.getUsedAt() != null) {
            throw new TokenAlreadyUsedException();
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }
        return token;
    }

    /**
     * Tra + kiểm tra hợp lệ 1 RefreshToken theo token gốc nhận từ cookie (refresh-token/logout
     * dùng chung logic này) — không tồn tại/đã revoke → TokenInvalidException, hết hạn →
     * TokenExpiredException.
     */
    private RefreshToken validateRefreshToken(String rawToken) {
        if (rawToken == null) {
            throw new TokenInvalidException();
        }
        RefreshToken token = refreshTokenRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(TokenInvalidException::new);
        if (token.isRevoked()) {
            throw new TokenInvalidException();
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }
        return token;
    }

    /**
     * Hash SHA-256 token trước khi lưu DB — chỉ trả token gốc (raw) cho client/log 1 lần
     * duy nhất lúc tạo, DB chỉ giữ bản hash để lộ DB không đồng nghĩa lộ token dùng được
     * ngay (giống cách lưu password, khác ở chỗ không cần salt vì token đã đủ ngẫu nhiên).
     * Endpoint verify-email/refresh-token (làm ở lượt sau) phải hash token nhận từ client
     * theo đúng cách này trước khi so khớp với tokenHash trong DB.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng trên JVM này", e);
        }
    }
}
