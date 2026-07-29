package com.languagelearning.language_learning_backend.lesson.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonCreateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonUpdateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.mapper.LessonMapper;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    private LessonServiceImpl lessonService;

    @BeforeEach
    void setUp() {
        LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);
        lessonService = new LessonServiceImpl(lessonRepository, courseRepository, lessonMapper);
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
        lesson.setTitle("Lesson 1");
        lesson.setStatus(LessonStatus.PUBLISHED);
        return lesson;
    }

    @Test
    void getPublishedLessonsByCourse_whenCoursePublished_returnsOnlyPublishedLessons() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse()));
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(publishedLesson(publishedCourse())));

        List<LessonSummaryResponse> result = lessonService.getPublishedLessonsByCourse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Lesson 1");
    }

    @Test
    void getPublishedLessonsByCourse_whenCourseDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        course.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> lessonService.getPublishedLessonsByCourse(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPublishedLessonsByCourse_whenCourseNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getPublishedLessonsByCourse(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPublishedLessonById_whenLessonAndCoursePublished_returnsResponse() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        LessonResponse response = lessonService.getPublishedLessonById(10L);

        assertThat(response.getTitle()).isEqualTo("Lesson 1");
        assertThat(response.getCourseId()).isEqualTo(1L);
    }

    @Test
    void getPublishedLessonById_whenLessonDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        lesson.setStatus(LessonStatus.DRAFT);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonService.getPublishedLessonById(10L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPublishedLessonById_whenParentCourseDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        course.setStatus(CourseStatus.DRAFT);
        Lesson lesson = publishedLesson(course);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonService.getPublishedLessonById(10L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createLesson_whenCourseExists_savesAndReturnsMapped() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse()));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(10L);
            return lesson;
        });

        LessonCreateRequest request = new LessonCreateRequest();
        request.setTitle("Lesson 1");

        LessonResponse response = lessonService.createLesson(1L, request);

        assertThat(response.getTitle()).isEqualTo("Lesson 1");
        assertThat(response.getCourseId()).isEqualTo(1L);
    }

    @Test
    void createLesson_whenCourseNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(1L, new LessonCreateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLesson_whenFound_updatesFields() {
        Lesson lesson = publishedLesson(publishedCourse());
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonUpdateRequest request = new LessonUpdateRequest();
        request.setTitle("Updated Lesson");
        request.setStatus(LessonStatus.DRAFT);

        LessonResponse response = lessonService.updateLesson(10L, request);

        assertThat(response.getTitle()).isEqualTo("Updated Lesson");
        assertThat(response.getStatus()).isEqualTo(LessonStatus.DRAFT);
    }

    @Test
    void deleteLesson_whenFound_softDeletes() {
        Lesson lesson = publishedLesson(publishedCourse());
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        lessonService.deleteLesson(10L);

        assertThat(lesson.isDeleted()).isTrue();
        assertThat(lesson.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteLesson_whenNotFound_throwsResourceNotFoundException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.deleteLesson(10L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
