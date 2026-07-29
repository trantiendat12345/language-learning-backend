package com.languagelearning.language_learning_backend.lesson.service.impl;

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
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryResponse> getPublishedLessonsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(ResourceNotFoundException::new);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException();
        }
        return lessonRepository.findAllByCourseIdAndStatusOrderByDisplayOrderAsc(courseId, LessonStatus.PUBLISHED)
                .stream()
                .map(lessonMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getPublishedLessonById(Long id) {
        Lesson lesson = findLessonOrThrow(id);
        if (lesson.getStatus() != LessonStatus.PUBLISHED || lesson.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            // Không tiết lộ Lesson/Course DRAFT tồn tại - trả cùng lỗi với id không tồn tại.
            throw new ResourceNotFoundException();
        }
        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryResponse> getLessonsByCourseForAdmin(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException();
        }
        return lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(courseId).stream()
                .map(lessonMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLessonByIdForAdmin(Long id) {
        return lessonMapper.toResponse(findLessonOrThrow(id));
    }

    @Override
    @Transactional
    public LessonResponse createLesson(Long courseId, LessonCreateRequest request) {
        Course course = courseRepository.findById(courseId).orElseThrow(ResourceNotFoundException::new);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setDisplayOrder(request.getDisplayOrder());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setAudioUrl(request.getAudioUrl());
        lesson.setEstimatedMinutes(request.getEstimatedMinutes());
        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long id, LessonUpdateRequest request) {
        Lesson lesson = findLessonOrThrow(id);
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setDisplayOrder(request.getDisplayOrder());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setAudioUrl(request.getAudioUrl());
        lesson.setEstimatedMinutes(request.getEstimatedMinutes());
        lesson.setStatus(request.getStatus());
        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = findLessonOrThrow(id);
        lesson.setDeleted(true);
        lesson.setDeletedAt(LocalDateTime.now());
        lessonRepository.save(lesson);
    }

    private Lesson findLessonOrThrow(Long id) {
        return lessonRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
