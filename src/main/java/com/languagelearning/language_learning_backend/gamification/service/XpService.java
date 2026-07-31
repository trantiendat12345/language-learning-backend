package com.languagelearning.language_learning_backend.gamification.service;

import com.languagelearning.language_learning_backend.gamification.enums.XpReason;

/**
 * D8 — cộng XP luôn phải cập nhật User.xp (denormalized) VÀ ghi 1 dòng XpLog cùng lúc, trong
 * cùng transaction (CLAUDE.md mục "Quy tắc bắt buộc" #9). Số lượng XP cho từng hành động
 * (amount) do nơi gọi tự quyết định — XpService chỉ đảm bảo tính atomic của việc ghi nhận,
 * không sở hữu bảng quy đổi XP.
 */
public interface XpService {

    void awardXp(Long userId, XpReason reason, int amount, Long sourceId);
}
