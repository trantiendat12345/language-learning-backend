package com.languagelearning.language_learning_backend.course.service.impl;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.course.dto.request.CourseCreateRequest;
import com.languagelearning.language_learning_backend.course.dto.request.CourseUpdateRequest;
import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseSummaryResponse;
import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.mapper.CourseMapper;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.course.repository.CourseSpecification;
import com.languagelearning.language_learning_backend.course.service.CourseService;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
import com.languagelearning.language_learning_backend.history.service.ActivityHistoryService;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.mapper.LessonMapper;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String SLUG_TAKEN_MESSAGE = "Slug đã tồn tại";

    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final LessonMapper lessonMapper;
    private final ActivityHistoryService activityHistoryService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getPublishedCourses(
            Long languageId, String difficulty, String keyword, Pageable pageable) {
        Specification<Course> spec = Specification.allOf(
                CourseSpecification.hasStatus(CourseStatus.PUBLISHED),
                CourseSpecification.hasLanguageId(languageId),
                CourseSpecification.hasDifficulty(difficulty),
                CourseSpecification.titleContains(keyword));
        Page<CourseSummaryResponse> page = courseRepository.findAll(spec, pageable).map(courseMapper::toSummaryResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public CourseResponse getPublishedCourseById(Long id, Long currentUserId) {
        Course course = courseRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            // Không tiết lộ Course DRAFT/ARCHIVED tồn tại - trả cùng lỗi với id không tồn tại.
            throw new ResourceNotFoundException();
        }
        List<Lesson> lessons = lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(
                id, LessonStatus.PUBLISHED);

        if (currentUserId != null) {
            activityHistoryService.recordActivity(currentUserId, ActivityTargetType.COURSE, id, ActivityAction.VIEWED);
        }

        return toCourseResponse(course, lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getAllCoursesForAdmin(Pageable pageable) {
        return PageResponse.from(courseRepository.findAll(pageable).map(courseMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseByIdForAdmin(Long id) {
        Course course = findCourseOrThrow(id);
        List<Lesson> lessons = lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(id);
        return toCourseResponse(course, lessons);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        if (courseRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException(SLUG_TAKEN_MESSAGE);
        }
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);

        Course course = new Course();
        course.setLanguage(language);
        course.setTitle(request.getTitle());
        course.setSlug(request.getSlug());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setDifficulty(request.getDifficulty());
        course.setEstimatedMinutes(request.getEstimatedMinutes());
        course.setDisplayOrder(request.getDisplayOrder());
        course = courseRepository.save(course);

        return toCourseResponse(course, List.of());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = findCourseOrThrow(id);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setDifficulty(request.getDifficulty());
        course.setEstimatedMinutes(request.getEstimatedMinutes());
        course.setDisplayOrder(request.getDisplayOrder());
        course.setStatus(request.getStatus());
        course = courseRepository.save(course);

        List<Lesson> lessons = lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(id);
        return toCourseResponse(course, lessons);
    }

    /** Soft-delete (D9) - xem comment tương tự ở LanguageServiceImpl về deletedBy. */
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = findCourseOrThrow(id);
        course.setDeleted(true);
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    private CourseResponse toCourseResponse(Course course, List<Lesson> lessons) {
        return courseMapper.toResponse(
                course, lessons.stream().map(lessonMapper::toSummaryResponse).toList());
    }
}
