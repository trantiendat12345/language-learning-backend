package com.languagelearning.language_learning_backend.gamification.service;

import com.languagelearning.language_learning_backend.user.entity.User;
import java.time.LocalDate;

/**
 * Cập nhật currentStreak/longestStreak/lastActiveDate trực tiếp trên User — KHÔNG có bảng
 * UserStreak riêng (quyết định chốt, xem Javadoc User entity). Chỉ gọi khi ghi nhận hoạt động
 * ĐẦU TIÊN trong ngày của user (do progress/service/DailyActivityService quyết định thời điểm
 * gọi), gọi nhiều lần trong cùng 1 activityDate phải là no-op để không cộng streak sai.
 */
public interface StreakService {

    void recordActivity(User user, LocalDate activityDate);
}
