package com.languagelearning.language_learning_backend.user.service;

import com.languagelearning.language_learning_backend.user.dto.request.ChangePasswordRequest;
import com.languagelearning.language_learning_backend.user.dto.request.UserUpdateRequest;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;

/**
 * Nghiệp vụ hồ sơ cá nhân — mọi method nhận `userId` lấy từ SecurityContext (không bao giờ
 * từ request param), chỉ thao tác trên đúng user đang gọi API (xem CLAUDE.md #6).
 */
public interface UserService {

    UserResponse getMyProfile(Long userId);

    UserResponse updateMyProfile(Long userId, UserUpdateRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
