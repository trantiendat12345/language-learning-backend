package com.languagelearning.language_learning_backend.gamification.service.impl;

import com.languagelearning.language_learning_backend.gamification.service.StreakService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements StreakService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordActivity(User user, LocalDate activityDate) {
        LocalDate lastActiveDate = user.getLastActiveDate();

        if (activityDate.equals(lastActiveDate)) {
            return;
        }

        if (lastActiveDate != null && lastActiveDate.equals(activityDate.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            // Mất chuỗi (hoặc lần hoạt động đầu tiên) - reset về 1, KHÔNG về 0, vì hôm nay
            // đã tính là 1 ngày có hoạt động (docs/testing/04_BUSINESS_RULES_GLOBAL.md mục 2).
            user.setCurrentStreak(1);
        }

        user.setLongestStreak(Math.max(user.getLongestStreak(), user.getCurrentStreak()));
        user.setLastActiveDate(activityDate);
        userRepository.save(user);
    }
}
