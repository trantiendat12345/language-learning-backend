package com.languagelearning.language_learning_backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GET /api/admin/dashboard - số liệu tổng quan (docs/testing/21_FRS_TC_ADMIN.md mục 1.4).
 * Toàn bộ số đếm loại trừ bản ghi đã soft-delete (JPA @SQLRestriction tự lọc is_deleted=false
 * cho Course/Lesson/Vocabulary/Deck/User, riêng QuizAttempt là Log/Transaction data không có
 * is_deleted - D9 - nên totalQuizAttempts đếm toàn bộ, không cần lọc riêng).
 * KHÔNG có "biểu đồ hoạt động học tập" - hoãn cùng "Biểu đồ thống kê nâng cao" (Phase 2, xem
 * docs/testing/02_FEATURE_LIST.md mục 9.11), chỉ mục 1.4 main flow FRS nhắc tới nhưng không có
 * Test Case nào đặc tả cụ thể, quyết định chốt khi code.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long totalCourses;
    private long totalLessons;
    private long totalVocabulary;
    private long totalDecks;
    private long totalQuizAttempts;
}
