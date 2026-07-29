package com.languagelearning.language_learning_backend.progress.service.impl;

import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.progress.dto.response.LessonCompleteResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.entity.LessonProgress;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
import com.languagelearning.language_learning_backend.progress.exception.CourseNotEnrolledException;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.progress.repository.LessonProgressRepository;
import com.languagelearning.language_learning_backend.progress.service.LessonProgressService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonProgressServiceImpl implements LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LessonCompleteResponse completeLesson(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(ResourceNotFoundException::new);
        if (lesson.getStatus() != LessonStatus.PUBLISHED || lesson.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException();
        }
        Long courseId = lesson.getCourse().getId();
        CourseEnrollment enrollment =
                courseEnrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow(CourseNotEnrolledException::new);

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId).orElse(null);
        if (progress == null) {
            User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
            progress = new LessonProgress();
            progress.setUser(user);
            progress.setLesson(lesson);
        }

        // Idempotent - hoàn thành lại Lesson đã COMPLETED không cộng thêm gì (XP đợi Giai đoạn 7).
        if (progress.getStatus() != LessonProgressStatus.COMPLETED) {
            progress.setStatus(LessonProgressStatus.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
            recalculateCourseProgress(enrollment, courseId, userId);
        }

        return LessonCompleteResponse.builder()
                .lessonId(lessonId)
                .lessonProgressStatus(progress.getStatus())
                .courseId(courseId)
                .courseProgressPercent(enrollment.getProgressPercent())
                .courseStatus(enrollment.getStatus())
                .build();
    }

    private void recalculateCourseProgress(CourseEnrollment enrollment, Long courseId, Long userId) {
        List<Long> publishedLessonIds = lessonRepository
                .findAllByCourseIdAndStatusOrderByDisplayOrderAsc(courseId, LessonStatus.PUBLISHED)
                .stream()
                .map(Lesson::getId)
                .toList();
        int total = publishedLessonIds.size();
        long completed = total == 0
                ? 0
                : lessonProgressRepository.countByUserIdAndStatusAndLessonIdIn(
                        userId, LessonProgressStatus.COMPLETED, publishedLessonIds);
        int progressPercent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);

        enrollment.setProgressPercent(progressPercent);
        if (progressPercent >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }
        enrollment.setUpdatedAt(LocalDateTime.now());
        courseEnrollmentRepository.save(enrollment);
    }
}
