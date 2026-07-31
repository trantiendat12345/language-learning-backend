package com.languagelearning.language_learning_backend.progress.service;

/**
 * Ghi nhận hoạt động học tập trong ngày của User (theo activityDate = ngày theo timezone User -
 * D10/CLAUDE.md #11). Được gọi từ Lesson complete/Quiz submit/Review submit - mỗi nơi tự quyết
 * định studyMinutesDelta/wordsLearnedDelta của hành động đó (xem từng ServiceImpl gọi vào đây).
 * Tự động: (1) gọi StreakService khi đây là hoạt động ĐẦU TIÊN trong ngày, (2) cộng XP
 * DAILY_GOAL_MET đúng 1 lần tại thời điểm goalMet chuyển từ false sang true.
 */
public interface DailyActivityService {

    void recordActivity(Long userId, int studyMinutesDelta, int wordsLearnedDelta);
}
