package com.languagelearning.language_learning_backend.progress.dto.response;

import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response cho GET /api/progress/dashboard (docs/testing/17_FRS_TC_PROGRESS_GAMIFICATION.md
 * mục 1.1). wordsToReviewCount PHẢI khớp chính xác kết quả GET /api/review/today (dùng chung
 * ReviewService.getTodayReviews - TC-PROG-001). recentQuizAccuracy/continueLearning nullable
 * khi user chưa làm Quiz lần nào / chưa enroll khoá học nào đang IN_PROGRESS. KHÔNG có
 * recentActivity/recommendedCourses - module History chưa build (Giai đoạn 8), chưa có thuật
 * toán gợi ý khoá học - phạm vi loại trừ tường minh cho chunk này.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDashboardResponse {

    private DailyGoalType dailyGoalType;
    private int dailyGoalValue;
    private int todayStudyMinutes;
    private int todayWordsLearned;
    private boolean goalMet;

    private int currentStreak;
    private int longestStreak;

    private int totalXp;

    private int wordsToReviewCount;

    private Float recentQuizAccuracy;

    private ContinueLearningResponse continueLearning;
}
