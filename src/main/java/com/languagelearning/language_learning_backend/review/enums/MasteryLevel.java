package com.languagelearning.language_learning_backend.review.enums;

/**
 * Suy ra từ `repetitionCount` (không phải input trực tiếp — xem FRS 15_FRS_TC_SRS_REVIEW.md
 * mục 1.3). Ngưỡng cụ thể (1/3/6) là quyết định chốt khi code — FRS không cho số chính xác,
 * chỉ yêu cầu "tăng dần đúng logic": NEW (chưa ôn thành công lần nào) → LEARNING (1-2 lần) →
 * FAMILIAR (3-5 lần) → MASTERED (≥ 6 lần), xem ReviewServiceImpl.computeMasteryLevel.
 */
public enum MasteryLevel {
    NEW,
    LEARNING,
    FAMILIAR,
    MASTERED
}
