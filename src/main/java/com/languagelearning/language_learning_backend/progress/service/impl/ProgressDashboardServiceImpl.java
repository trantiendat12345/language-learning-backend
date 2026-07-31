package com.languagelearning.language_learning_backend.progress.service.impl;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.progress.dto.response.ContinueLearningResponse;
import com.languagelearning.language_learning_backend.progress.dto.response.ProgressDashboardResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.entity.UserDailyActivity;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.progress.repository.LessonProgressRepository;
import com.languagelearning.language_learning_backend.progress.repository.UserDailyActivityRepository;
import com.languagelearning.language_learning_backend.progress.service.ProgressDashboardService;
import com.languagelearning.language_learning_backend.quiz.entity.QuizAttempt;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.review.service.ReviewService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressDashboardServiceImpl implements ProgressDashboardService {

    private final UserRepository userRepository;
    private final UserDailyActivityRepository userDailyActivityRepository;
    private final ReviewService reviewService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public ProgressDashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        LocalDate today = LocalDate.now(ZoneId.of(user.getTimezone()));

        UserDailyActivity todayActivity = userDailyActivityRepository
                .findByUserIdAndActivityDate(userId, today)
                .orElse(null);

        int wordsToReviewCount = reviewService.getTodayReviews(userId).size();

        Float recentQuizAccuracy = quizAttemptRepository
                .findAllByUserIdOrderByCompletedAtDesc(userId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(QuizAttempt::getAccuracy)
                .orElse(null);

        return ProgressDashboardResponse.builder()
                .dailyGoalType(user.getDailyGoalType())
                .dailyGoalValue(user.getDailyGoalValue())
                .todayStudyMinutes(todayActivity == null ? 0 : todayActivity.getStudyMinutes())
                .todayWordsLearned(todayActivity == null ? 0 : todayActivity.getWordsLearned())
                .goalMet(todayActivity != null && todayActivity.isGoalMet())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .totalXp(user.getXp())
                .wordsToReviewCount(wordsToReviewCount)
                .recentQuizAccuracy(recentQuizAccuracy)
                .continueLearning(findContinueLearning(userId))
                .build();
    }

    private ContinueLearningResponse findContinueLearning(Long userId) {
        CourseEnrollment enrollment = courseEnrollmentRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId, EnrollmentStatus.IN_PROGRESS)
                .orElse(null);
        if (enrollment == null) {
            return null;
        }

        Course course = enrollment.getCourse();
        List<Lesson> publishedLessons =
                lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(course.getId(), LessonStatus.PUBLISHED);
        if (publishedLessons.isEmpty()) {
            return null;
        }

        Set<Long> completedLessonIds = lessonProgressRepository
                .findAllByUserIdAndLessonIdInAndStatus(
                        userId, publishedLessons.stream().map(Lesson::getId).toList(), LessonProgressStatus.COMPLETED)
                .stream()
                .map(lp -> lp.getLesson().getId())
                .collect(Collectors.toSet());

        Lesson nextLesson = publishedLessons.stream()
                .filter(lesson -> !completedLessonIds.contains(lesson.getId()))
                .findFirst()
                .orElse(null);
        if (nextLesson == null) {
            return null;
        }

        return ContinueLearningResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .lessonId(nextLesson.getId())
                .lessonTitle(nextLesson.getTitle())
                .build();
    }
}
