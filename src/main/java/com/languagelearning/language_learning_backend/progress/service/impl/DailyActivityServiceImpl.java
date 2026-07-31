package com.languagelearning.language_learning_backend.progress.service.impl;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.gamification.enums.XpReason;
import com.languagelearning.language_learning_backend.gamification.service.StreakService;
import com.languagelearning.language_learning_backend.gamification.service.XpService;
import com.languagelearning.language_learning_backend.progress.entity.UserDailyActivity;
import com.languagelearning.language_learning_backend.progress.repository.UserDailyActivityRepository;
import com.languagelearning.language_learning_backend.progress.service.DailyActivityService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyActivityServiceImpl implements DailyActivityService {

    /** Số XP thưởng khi đạt Daily Goal trong ngày - quyết định chốt khi code (FRS chỉ liệt kê reason, không cho số chính xác, xem docs/testing/04_BUSINESS_RULES_GLOBAL.md mục 1). */
    private static final int DAILY_GOAL_MET_XP = 20;

    private final UserDailyActivityRepository userDailyActivityRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final XpService xpService;

    @Override
    @Transactional
    public void recordActivity(Long userId, int studyMinutesDelta, int wordsLearnedDelta) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        LocalDate activityDate = LocalDate.now(ZoneId.of(user.getTimezone()));

        UserDailyActivity activity = userDailyActivityRepository
                .findByUserIdAndActivityDate(userId, activityDate)
                .orElseGet(() -> {
                    streakService.recordActivity(user, activityDate);
                    UserDailyActivity created = new UserDailyActivity();
                    created.setUser(user);
                    created.setActivityDate(activityDate);
                    return created;
                });

        activity.setStudyMinutes(activity.getStudyMinutes() + studyMinutesDelta);
        activity.setWordsLearned(activity.getWordsLearned() + wordsLearnedDelta);

        boolean wasGoalMet = activity.isGoalMet();
        boolean isGoalMet = isGoalMet(user, activity);
        if (isGoalMet && !wasGoalMet) {
            xpService.awardXp(userId, XpReason.DAILY_GOAL_MET, DAILY_GOAL_MET_XP, null);
            activity.setXpEarned(activity.getXpEarned() + DAILY_GOAL_MET_XP);
        }
        activity.setGoalMet(isGoalMet);

        userDailyActivityRepository.save(activity);
    }

    private boolean isGoalMet(User user, UserDailyActivity activity) {
        if (user.getDailyGoalType() == DailyGoalType.TIME) {
            return activity.getStudyMinutes() >= user.getDailyGoalValue();
        }
        return activity.getWordsLearned() >= user.getDailyGoalValue();
    }
}
