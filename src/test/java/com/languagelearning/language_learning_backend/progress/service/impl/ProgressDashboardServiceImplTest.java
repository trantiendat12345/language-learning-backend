package com.languagelearning.language_learning_backend.progress.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.progress.dto.response.ProgressDashboardResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.entity.LessonProgress;
import com.languagelearning.language_learning_backend.progress.entity.UserDailyActivity;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.progress.repository.LessonProgressRepository;
import com.languagelearning.language_learning_backend.progress.repository.UserDailyActivityRepository;
import com.languagelearning.language_learning_backend.quiz.entity.QuizAttempt;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewTodayItemResponse;
import com.languagelearning.language_learning_backend.review.service.ReviewService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProgressDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDailyActivityRepository userDailyActivityRepository;

    @Mock
    private ReviewService reviewService;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    private ProgressDashboardServiceImpl progressDashboardService;

    @BeforeEach
    void setUp() {
        progressDashboardService = new ProgressDashboardServiceImpl(
                userRepository,
                userDailyActivityRepository,
                reviewService,
                quizAttemptRepository,
                courseEnrollmentRepository,
                lessonRepository,
                lessonProgressRepository);
    }

    private User user() {
        User user = new User();
        user.setId(100L);
        user.setTimezone("UTC");
        user.setDailyGoalType(DailyGoalType.WORDS);
        user.setDailyGoalValue(10);
        user.setCurrentStreak(3);
        user.setLongestStreak(7);
        user.setXp(120);
        return user;
    }

    private void stubNoQuizAttemptNoContinueLearning(User user) {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.findAllByUserIdOrderByCompletedAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(courseEnrollmentRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(100L, EnrollmentStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
    }

    @Test
    void getDashboard_wordsToReviewCount_matchesReviewServiceSize() {
        User user = user();
        stubNoQuizAttemptNoContinueLearning(user);
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L))
                .thenReturn(List.of(
                        ReviewTodayItemResponse.builder().vocabularyId(1L).build(),
                        ReviewTodayItemResponse.builder().vocabularyId(2L).build()));

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getWordsToReviewCount()).isEqualTo(2);
    }

    @Test
    void getDashboard_noTodayActivity_returnsZeroedGoalProgress() {
        User user = user();
        stubNoQuizAttemptNoContinueLearning(user);
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getTodayStudyMinutes()).isZero();
        assertThat(response.getTodayWordsLearned()).isZero();
        assertThat(response.isGoalMet()).isFalse();
        assertThat(response.getDailyGoalType()).isEqualTo(DailyGoalType.WORDS);
        assertThat(response.getDailyGoalValue()).isEqualTo(10);
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getLongestStreak()).isEqualTo(7);
        assertThat(response.getTotalXp()).isEqualTo(120);
    }

    @Test
    void getDashboard_hasTodayActivity_reflectsStudyMinutesAndGoalMet() {
        User user = user();
        stubNoQuizAttemptNoContinueLearning(user);
        UserDailyActivity activity = new UserDailyActivity();
        activity.setStudyMinutes(15);
        activity.setWordsLearned(10);
        activity.setGoalMet(true);
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.of(activity));
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getTodayStudyMinutes()).isEqualTo(15);
        assertThat(response.getTodayWordsLearned()).isEqualTo(10);
        assertThat(response.isGoalMet()).isTrue();
    }

    @Test
    void getDashboard_noQuizAttempts_recentQuizAccuracyIsNull() {
        User user = user();
        stubNoQuizAttemptNoContinueLearning(user);
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getRecentQuizAccuracy()).isNull();
    }

    @Test
    void getDashboard_hasQuizAttempts_returnsLatestAccuracy() {
        User user = user();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());
        QuizAttempt latest = new QuizAttempt();
        latest.setAccuracy(80f);
        when(quizAttemptRepository.findAllByUserIdOrderByCompletedAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(latest)));
        when(courseEnrollmentRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(100L, EnrollmentStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getRecentQuizAccuracy()).isEqualTo(80f);
    }

    @Test
    void getDashboard_noInProgressEnrollment_continueLearningIsNull() {
        User user = user();
        stubNoQuizAttemptNoContinueLearning(user);
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getContinueLearning()).isNull();
    }

    @Test
    void getDashboard_withInProgressEnrollment_returnsFirstIncompletePublishedLesson() {
        User user = user();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());
        when(quizAttemptRepository.findAllByUserIdOrderByCompletedAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(Page.empty());

        Course course = new Course();
        course.setId(1L);
        course.setTitle("English A1");
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        when(courseEnrollmentRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(100L, EnrollmentStatus.IN_PROGRESS))
                .thenReturn(Optional.of(enrollment));

        Lesson lesson1 = new Lesson();
        lesson1.setId(10L);
        lesson1.setTitle("Bài 1");
        lesson1.setStatus(LessonStatus.PUBLISHED);
        Lesson lesson2 = new Lesson();
        lesson2.setId(11L);
        lesson2.setTitle("Bài 2");
        lesson2.setStatus(LessonStatus.PUBLISHED);
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson1, lesson2));

        LessonProgress completedProgress = new LessonProgress();
        completedProgress.setLesson(lesson1);
        when(lessonProgressRepository.findAllByUserIdAndLessonIdInAndStatus(
                        100L, List.of(10L, 11L), LessonProgressStatus.COMPLETED))
                .thenReturn(List.of(completedProgress));

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getContinueLearning()).isNotNull();
        assertThat(response.getContinueLearning().getCourseId()).isEqualTo(1L);
        assertThat(response.getContinueLearning().getLessonId()).isEqualTo(11L);
        assertThat(response.getContinueLearning().getLessonTitle()).isEqualTo("Bài 2");
    }

    @Test
    void getDashboard_allPublishedLessonsCompleted_continueLearningIsNull() {
        User user = user();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userDailyActivityRepository.findByUserIdAndActivityDate(any(), any())).thenReturn(Optional.empty());
        when(reviewService.getTodayReviews(100L)).thenReturn(List.of());
        when(quizAttemptRepository.findAllByUserIdOrderByCompletedAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(Page.empty());

        Course course = new Course();
        course.setId(1L);
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        when(courseEnrollmentRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(100L, EnrollmentStatus.IN_PROGRESS))
                .thenReturn(Optional.of(enrollment));

        Lesson lesson1 = new Lesson();
        lesson1.setId(10L);
        lesson1.setStatus(LessonStatus.PUBLISHED);
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson1));

        LessonProgress completedProgress = new LessonProgress();
        completedProgress.setLesson(lesson1);
        when(lessonProgressRepository.findAllByUserIdAndLessonIdInAndStatus(100L, List.of(10L), LessonProgressStatus.COMPLETED))
                .thenReturn(List.of(completedProgress));

        ProgressDashboardResponse response = progressDashboardService.getDashboard(100L);

        assertThat(response.getContinueLearning()).isNull();
    }
}
