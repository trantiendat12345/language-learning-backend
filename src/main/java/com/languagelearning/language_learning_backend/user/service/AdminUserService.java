package com.languagelearning.language_learning_backend.user.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.user.dto.response.AdminUserProgressResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * Quản lý User bởi Admin — tách riêng khỏi UserService (dùng cho hồ sơ cá nhân currentUser)
 * vì ngữ nghĩa khác hẳn: UserService luôn lấy currentUserId từ SecurityContext để thao tác
 * trên chính bản ghi của người gọi, còn AdminUserService thao tác trên userId bất kỳ truyền
 * vào (chỉ ADMIN mới gọi được, xem SecurityConfig `/api/admin/**`).
 */
public interface AdminUserService {

    PageResponse<UserResponse> getUsers(String keyword, Pageable pageable);

    UserResponse getUserById(Long userId);

    UserResponse activateUser(Long userId);

    /** Revoke toàn bộ RefreshToken của user ngay lập tức. Chặn Admin tự disable chính mình (rủi ro tự khoá hệ thống). */
    UserResponse disableUser(Long userId, Long currentAdminId);

    /** Revoke toàn bộ RefreshToken của user ngay lập tức. Chặn Admin tự lock chính mình. */
    UserResponse lockUser(Long userId, Long currentAdminId);

    /** XP/Streak + danh sách CourseEnrollment của user, dùng cho Admin xem tiến độ học. */
    AdminUserProgressResponse getUserProgress(Long userId);
}
