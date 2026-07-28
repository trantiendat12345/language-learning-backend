package com.languagelearning.language_learning_backend.user.service.impl;

import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import com.languagelearning.language_learning_backend.auth.exception.InvalidCredentialsException;
import com.languagelearning.language_learning_backend.auth.exception.NewPasswordSameAsCurrentException;
import com.languagelearning.language_learning_backend.auth.exception.PasswordMismatchException;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.user.dto.request.ChangePasswordRequest;
import com.languagelearning.language_learning_backend.user.dto.request.UserUpdateRequest;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nghiệp vụ hồ sơ cá nhân — xem UserService để biết quy tắc currentUserId từ SecurityContext.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(Long userId) {
        return userMapper.toResponse(findUserOrThrow(userId));
    }

    /**
     * Chỉ cho sửa displayName/avatarUrl/birthday/gender/country/currentLevel — DTO
     * UserUpdateRequest cố tình không có username/email/password/xp/coin/role/status nên
     * client không thể đổi các field đó qua endpoint này (xem
     * docs/testing/12_FRS_TC_USER_PROFILE.md mục 1.2).
     */
    @Override
    @Transactional
    public UserResponse updateMyProfile(Long userId, UserUpdateRequest request) {
        User user = findUserOrThrow(userId);
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setBirthday(request.getBirthday());
        user.setGender(request.getGender());
        user.setCountry(request.getCountry());
        user.setCurrentLevel(request.getCurrentLevel());
        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * Yêu cầu xác thực currentPassword đúng trước khi đổi (khác Reset Password dùng token
     * gửi qua email). newPassword trùng currentPassword bị từ chối — quyết định chốt khi
     * code cho mục "cần xác nhận" ở docs/testing/12_FRS_TC_USER_PROFILE.md mục 1.3. Sau khi
     * đổi thành công, revoke toàn bộ Refresh Token cũ (cùng nguyên tắc Reset Password).
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        User user = findUserOrThrow(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new NewPasswordSameAsCurrentException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        activeTokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
    }
}
