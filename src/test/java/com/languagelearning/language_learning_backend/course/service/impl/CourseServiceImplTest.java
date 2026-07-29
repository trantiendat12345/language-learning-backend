package com.languagelearning.language_learning_backend.course.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.dto.request.CourseCreateRequest;
import com.languagelearning.language_learning_backend.course.dto.request.CourseUpdateRequest;
import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.mapper.CourseMapper;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LessonRepository lessonRepository;

    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);
        LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);
        courseService = new CourseServiceImpl(
                courseRepository, languageRepository, lessonRepository, courseMapper, lessonMapper);
    }

    private Language language() {
        Language language = new Language();
        language.setId(1L);
        language.setCode("en");
        language.setName("English");
        return language;
    }

    private Course publishedCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setLanguage(language());
        course.setTitle("English Beginner A1");
        course.setSlug("english-beginner-a1");
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    @Test
    void getPublishedCourses_returnsMappedPage() {
        Page<Course> page = new PageImpl<>(List.of(publishedCourse()));
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = courseService.getPublishedCourses(null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLanguageCode()).isEqualTo("en");
    }

    @Test
    void getPublishedCourseById_whenPublished_returnsResponseWithLessons() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse()));
        Lesson lesson = new Lesson();
        lesson.setId(10L);
        lesson.setTitle("Lesson 1");
        lesson.setStatus(LessonStatus.PUBLISHED);
        when(lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(1L, LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson));

        CourseResponse response = courseService.getPublishedCourseById(1L);

        assertThat(response.getLessons()).hasSize(1);
        assertThat(response.getLessons().get(0).getTitle()).isEqualTo("Lesson 1");
    }

    @Test
    void getPublishedCourseById_whenDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        course.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.getPublishedCourseById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPublishedCourseById_whenNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getPublishedCourseById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCourseByIdForAdmin_returnsRegardlessOfStatus() {
        Course course = publishedCourse();
        course.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        CourseResponse response = courseService.getCourseByIdForAdmin(1L);

        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    private CourseCreateRequest createRequest() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setLanguageId(1L);
        request.setTitle("English Beginner A1");
        request.setSlug("english-beginner-a1");
        return request;
    }

    @Test
    void createCourse_withNewSlug_savesAndReturnsMapped() {
        CourseCreateRequest request = createRequest();
        when(courseRepository.existsBySlug("english-beginner-a1")).thenReturn(false);
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(1L);
            return course;
        });

        CourseResponse response = courseService.createCourse(request);

        assertThat(response.getSlug()).isEqualTo("english-beginner-a1");
        assertThat(response.getLanguageCode()).isEqualTo("en");
    }

    @Test
    void createCourse_whenSlugAlreadyExists_throwsDuplicateResourceException() {
        CourseCreateRequest request = createRequest();
        when(courseRepository.existsBySlug("english-beginner-a1")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(request)).isInstanceOf(DuplicateResourceException.class);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createCourse_whenLanguageNotFound_throwsResourceNotFoundException() {
        CourseCreateRequest request = createRequest();
        when(courseRepository.existsBySlug("english-beginner-a1")).thenReturn(false);
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(request)).isInstanceOf(ResourceNotFoundException.class);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void updateCourse_whenFound_updatesFields() {
        Course course = publishedCourse();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Updated Title");
        request.setStatus(CourseStatus.ARCHIVED);

        CourseResponse response = courseService.updateCourse(1L, request);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getStatus()).isEqualTo(CourseStatus.ARCHIVED);
    }

    @Test
    void deleteCourse_whenFound_softDeletes() {
        Course course = publishedCourse();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.deleteCourse(1L);

        assertThat(course.isDeleted()).isTrue();
        assertThat(course.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteCourse_whenNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
