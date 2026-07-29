package com.languagelearning.language_learning_backend.progress.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.mapper.CourseEnrollmentMapper;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentServiceImplTest {

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    private CourseEnrollmentServiceImpl courseEnrollmentService;

    @BeforeEach
    void setUp() {
        CourseEnrollmentMapper mapper = Mappers.getMapper(CourseEnrollmentMapper.class);
        courseEnrollmentService =
                new CourseEnrollmentServiceImpl(courseEnrollmentRepository, courseRepository, userRepository, mapper);
    }

    private Course publishedCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("English Beginner A1");
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    private User user() {
        User user = new User();
        user.setId(100L);
        return user;
    }

    @Test
    void enrollInCourse_whenNotYetEnrolled_createsNewEnrollment() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse()));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(courseEnrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(invocation -> {
            CourseEnrollment enrollment = invocation.getArgument(0);
            enrollment.setId(1L);
            return enrollment;
        });

        CourseEnrollmentResponse response = courseEnrollmentService.enrollInCourse(1L, 100L);

        assertThat(response.getCourseId()).isEqualTo(1L);
        assertThat(response.getCourseTitle()).isEqualTo("English Beginner A1");
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.IN_PROGRESS);
        assertThat(response.getProgressPercent()).isZero();
    }

    @Test
    void enrollInCourse_whenAlreadyEnrolled_returnsExistingWithoutCreatingNew() {
        Course course = publishedCourse();
        CourseEnrollment existing = new CourseEnrollment();
        existing.setId(5L);
        existing.setCourse(course);
        existing.setStatus(EnrollmentStatus.IN_PROGRESS);
        existing.setProgressPercent(50);
        existing.setEnrolledAt(LocalDateTime.now());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.findByUserIdAndCourseId(100L, 1L)).thenReturn(Optional.of(existing));

        CourseEnrollmentResponse response = courseEnrollmentService.enrollInCourse(1L, 100L);

        assertThat(response.getProgressPercent()).isEqualTo(50);
        verify(courseEnrollmentRepository, never()).save(any());
    }

    @Test
    void enrollInCourse_whenCourseDraft_throwsResourceNotFoundException() {
        Course course = publishedCourse();
        course.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseEnrollmentService.enrollInCourse(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enrollInCourse_whenCourseNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseEnrollmentService.enrollInCourse(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
