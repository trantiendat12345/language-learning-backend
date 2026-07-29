package com.languagelearning.language_learning_backend.progress.service.impl;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.mapper.CourseEnrollmentMapper;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.progress.service.CourseEnrollmentService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentMapper courseEnrollmentMapper;

    @Override
    @Transactional
    public CourseEnrollmentResponse enrollInCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId).orElseThrow(ResourceNotFoundException::new);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException();
        }

        CourseEnrollment enrollment = courseEnrollmentRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> createEnrollment(userId, course));
        return courseEnrollmentMapper.toResponse(enrollment);
    }

    private CourseEnrollment createEnrollment(Long userId, Course course) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.IN_PROGRESS);
        enrollment.setProgressPercent(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return courseEnrollmentRepository.save(enrollment);
    }
}
