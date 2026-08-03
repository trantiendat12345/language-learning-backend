package com.languagelearning.language_learning_backend.user.dto.response;

import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** GET /api/admin/users/{id}/progress - Admin xem tiến độ học của 1 User cụ thể (docs/testing/21_FRS_TC_ADMIN.md mục 1.3). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserProgressResponse {

    private Long userId;
    private String username;
    private int xp;
    private int currentStreak;
    private int longestStreak;
    private List<CourseEnrollmentResponse> courseEnrollments;
}
