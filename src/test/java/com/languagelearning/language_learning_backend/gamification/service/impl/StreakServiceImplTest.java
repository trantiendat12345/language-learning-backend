package com.languagelearning.language_learning_backend.gamification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StreakServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private StreakServiceImpl streakService;

    @BeforeEach
    void setUp() {
        streakService = new StreakServiceImpl(userRepository);
    }

    private User user(int currentStreak, int longestStreak, LocalDate lastActiveDate) {
        User user = new User();
        user.setId(100L);
        user.setCurrentStreak(currentStreak);
        user.setLongestStreak(longestStreak);
        user.setLastActiveDate(lastActiveDate);
        return user;
    }

    @Test
    void recordActivity_firstEverActivity_setsStreakTo1() {
        User user = user(0, 0, null);
        LocalDate today = LocalDate.of(2026, 7, 30);

        streakService.recordActivity(user, today);

        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getLongestStreak()).isEqualTo(1);
        assertThat(user.getLastActiveDate()).isEqualTo(today);
        verify(userRepository).save(user);
    }

    @Test
    void recordActivity_yesterdayHadActivity_incrementsStreak() {
        LocalDate today = LocalDate.of(2026, 7, 30);
        User user = user(5, 5, today.minusDays(1));

        streakService.recordActivity(user, today);

        assertThat(user.getCurrentStreak()).isEqualTo(6);
        assertThat(user.getLongestStreak()).isEqualTo(6);
    }

    @Test
    void recordActivity_missedADay_resetsStreakTo1NotZero() {
        LocalDate today = LocalDate.of(2026, 7, 30);
        User user = user(10, 10, today.minusDays(3));

        streakService.recordActivity(user, today);

        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getLongestStreak()).isEqualTo(10);
    }

    @Test
    void recordActivity_longestStreakNeverDecreases() {
        LocalDate today = LocalDate.of(2026, 7, 30);
        User user = user(1, 10, today.minusDays(5));

        streakService.recordActivity(user, today);

        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getLongestStreak()).isEqualTo(10);
    }

    @Test
    void recordActivity_calledTwiceSameDay_isNoOp() {
        LocalDate today = LocalDate.of(2026, 7, 30);
        User user = user(3, 3, today);

        streakService.recordActivity(user, today);

        assertThat(user.getCurrentStreak()).isEqualTo(3);
        verify(userRepository, never()).save(user);
    }
}
