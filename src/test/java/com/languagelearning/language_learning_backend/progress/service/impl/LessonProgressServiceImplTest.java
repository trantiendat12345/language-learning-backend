package com.languagelearning.language_learning_backend.progress.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
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
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonProgressServiceImplTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private UserRepository userRepository;

    private LessonProgressServiceImpl lessonProgressService;

    @BeforeEach
    void setUp() {
        lessonProgressService = new LessonProgressServiceImpl(
                lessonProgressRepository, lessonRepository, courseEnrollmentRepository, userRepository);
    }

    private Course publishedCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    private Lesson publishedLesson(Course course) {
        Lesson lesson = new Lesson();
        lesson.setId(10L);
        lesson.setCourse(course);
        lesson.setStatus(LessonStatus.PUBLISHED);
        return lesson;
    }

    private CourseEnrollment enrollment() {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(1L);
        enrollment.setStatus(EnrollmentStatus.IN_PROGRESS);
        enrollment.setProgressPercent(0);
        return enrollment;
    }

    @Test
    void completeLesson_whenFirstTime_createsProgressAndUpdatesEnrollmentPercent() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        Lesson lesson2 = publishedLesson(course);
        lesson2.setId(11L);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.of(enrollment()));
        when(lessonProgressRepository.findByUserIdAndLessonId(100L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson, lesson2));
        when(lessonProgressRepository.countByUserIdAndStatusAndLessonIdIn(100L, LessonProgressStatus.COMPLETED, List.of(10L, 11L)))
                .thenReturn(1L);
        when(courseEnrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonCompleteResponse response = lessonProgressService.completeLesson(10L, 100L);

        assertThat(response.getLessonProgressStatus()).isEqualTo(LessonProgressStatus.COMPLETED);
        assertThat(response.getCourseProgressPercent()).isEqualTo(50);
        assertThat(response.getCourseStatus()).isEqualTo(EnrollmentStatus.IN_PROGRESS);
    }

    @Test
    void completeLesson_whenAllPublishedLessonsCompleted_marksCourseCompleted() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.of(enrollment()));
        when(lessonProgressRepository.findByUserIdAndLessonId(100L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson));
        when(lessonProgressRepository.countByUserIdAndStatusAndLessonIdIn(100L, LessonProgressStatus.COMPLETED, List.of(10L)))
                .thenReturn(1L);
        when(courseEnrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonCompleteResponse response = lessonProgressService.completeLesson(10L, 100L);

        assertThat(response.getCourseProgressPercent()).isEqualTo(100);
        assertThat(response.getCourseStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void completeLesson_whenAlreadyCompleted_idempotentDoesNotRecalculate() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        LessonProgress existingProgress = new LessonProgress();
        existingProgress.setStatus(LessonProgressStatus.COMPLETED);
        CourseEnrollment enrollment = enrollment();
        enrollment.setProgressPercent(100);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByUserIdAndLessonId(100L, 10L)).thenReturn(Optional.of(existingProgress));

        LessonCompleteResponse response = lessonProgressService.completeLesson(10L, 100L);

        assertThat(response.getCourseProgressPercent()).isEqualTo(100);
        verify(lessonProgressRepository, never()).save(any());
        verify(courseEnrollmentRepository, never()).save(any());
    }

    @Test
    void completeLesson_whenNotEnrolled_throwsCourseNotEnrolledException() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonProgressService.completeLesson(10L, 100L))
                .isInstanceOf(CourseNotEnrolledException.class);
    }

    @Test
    void completeLesson_whenLessonDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        lesson.setStatus(LessonStatus.DRAFT);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonProgressService.completeLesson(10L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void completeLesson_whenLessonNotFound_throwsResourceNotFoundException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonProgressService.completeLesson(10L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
