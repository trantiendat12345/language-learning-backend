package com.languagelearning.language_learning_backend.user.service.impl;

import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;
import com.languagelearning.language_learning_backend.progress.mapper.CourseEnrollmentMapper;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.user.dto.response.AdminUserProgressResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.user.repository.UserSpecification;
import com.languagelearning.language_learning_backend.user.service.AdminUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final String CANNOT_MODIFY_OWN_STATUS_MESSAGE =
            "Không thể tự thay đổi trạng thái tài khoản ADMIN của chính mình";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseEnrollmentMapper courseEnrollmentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String keyword, Pageable pageable) {
        Specification<User> spec = Specification.allOf(UserSpecification.usernameOrEmailContains(keyword));
        Page<UserResponse> page = userRepository.findAll(spec, pageable).map(userMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(findUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse disableUser(Long userId, Long currentAdminId) {
        requireNotSelf(userId, currentAdminId);
        User user = findUserOrThrow(userId);
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
        revokeAllRefreshTokens(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse lockUser(Long userId, Long currentAdminId) {
        requireNotSelf(userId, currentAdminId);
        User user = findUserOrThrow(userId);
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
        revokeAllRefreshTokens(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserProgressResponse getUserProgress(Long userId) {
        User user = findUserOrThrow(userId);
        List<CourseEnrollmentResponse> enrollments = courseEnrollmentRepository.findAllByUserId(userId).stream()
                .map(courseEnrollmentMapper::toResponse)
                .toList();

        return AdminUserProgressResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .xp(user.getXp())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .courseEnrollments(enrollments)
                .build();
    }

    /** Chặn Admin tự disable/lock chính mình - tránh tự khoá bản thân ra khỏi hệ thống (docs/testing/21_FRS_TC_ADMIN.md mục 1.3). */
    private void requireNotSelf(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new BadRequestException(CANNOT_MODIFY_OWN_STATUS_MESSAGE);
        }
    }

    private void revokeAllRefreshTokens(Long userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        activeTokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
    }
}
