package com.languagelearning.language_learning_backend.progress.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.gamification.enums.XpReason;
import com.languagelearning.language_learning_backend.gamification.service.StreakService;
import com.languagelearning.language_learning_backend.gamification.service.XpService;
import com.languagelearning.language_learning_backend.progress.entity.UserDailyActivity;
import com.languagelearning.language_learning_backend.progress.repository.UserDailyActivityRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyActivityServiceImplTest {

    @Mock
    private UserDailyActivityRepository userDailyActivityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StreakService streakService;

    @Mock
    private XpService xpService;

    private DailyActivityServiceImpl dailyActivityService;

    @BeforeEach
    void setUp() {
        dailyActivityService =
                new DailyActivityServiceImpl(userDailyActivityRepository, userRepository, streakService, xpService);
    }

    private User user(DailyGoalType goalType, int goalValue) {
        User user = new User();
        user.setId(100L);
        user.setTimezone("UTC");
        user.setDailyGoalType(goalType);
        user.setDailyGoalValue(goalValue);
        return user;
    }

    @Test
    void recordActivity_firstActivityOfDay_createsRowAndCallsStreakService() {
        User user = user(DailyGoalType.WORDS, 10);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(userDailyActivityRepository.save(any(UserDailyActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dailyActivityService.recordActivity(100L, 5, 2);

        verify(streakService).recordActivity(eq(user), any(LocalDate.class));
        ArgumentCaptor<UserDailyActivity> captor = ArgumentCaptor.forClass(UserDailyActivity.class);
        verify(userDailyActivityRepository).save(captor.capture());
        assertThat(captor.getValue().getStudyMinutes()).isEqualTo(5);
        assertThat(captor.getValue().getWordsLearned()).isEqualTo(2);
    }

    @Test
    void recordActivity_secondActivitySameDay_doesNotCallStreakServiceAgain() {
        User user = user(DailyGoalType.WORDS, 10);
        UserDailyActivity existing = new UserDailyActivity();
        existing.setUser(user);
        existing.setStudyMinutes(3);
        existing.setWordsLearned(2);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(userDailyActivityRepository.save(any(UserDailyActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dailyActivityService.recordActivity(100L, 5, 1);

        verify(streakService, never()).recordActivity(any(), any());
        assertThat(existing.getStudyMinutes()).isEqualTo(8);
        assertThat(existing.getWordsLearned()).isEqualTo(3);
    }

    @Test
    void recordActivity_wordsGoalJustMet_awardsDailyGoalMetXpOnce() {
        User user = user(DailyGoalType.WORDS, 5);
        UserDailyActivity existing = new UserDailyActivity();
        existing.setUser(user);
        existing.setWordsLearned(4);
        existing.setGoalMet(false);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(userDailyActivityRepository.save(any(UserDailyActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dailyActivityService.recordActivity(100L, 0, 1);

        assertThat(existing.isGoalMet()).isTrue();
        verify(xpService).awardXp(100L, XpReason.DAILY_GOAL_MET, 20, null);
    }

    @Test
    void recordActivity_goalAlreadyMet_doesNotAwardXpAgain() {
        User user = user(DailyGoalType.WORDS, 5);
        UserDailyActivity existing = new UserDailyActivity();
        existing.setUser(user);
        existing.setWordsLearned(5);
        existing.setGoalMet(true);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(userDailyActivityRepository.save(any(UserDailyActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dailyActivityService.recordActivity(100L, 0, 1);

        verify(xpService, never()).awardXp(any(), eq(XpReason.DAILY_GOAL_MET), any(Integer.class), any());
    }

    @Test
    void recordActivity_timeGoalType_checksStudyMinutesNotWords() {
        User user = user(DailyGoalType.TIME, 20);
        UserDailyActivity existing = new UserDailyActivity();
        existing.setUser(user);
        existing.setStudyMinutes(15);
        existing.setWordsLearned(100);
        existing.setGoalMet(false);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(userDailyActivityRepository.save(any(UserDailyActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dailyActivityService.recordActivity(100L, 4, 0);

        assertThat(existing.isGoalMet()).isFalse();
        verify(xpService, never()).awardXp(any(), any(), any(Integer.class), any());
    }
}
