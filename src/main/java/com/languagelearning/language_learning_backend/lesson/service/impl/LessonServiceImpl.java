package com.languagelearning.language_learning_backend.lesson.service.impl;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.mapper.GrammarMapper;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarExampleRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonCreateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonUpdateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonVocabularyAttachRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonVocabularyResponse;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.entity.LessonVocabulary;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.mapper.LessonMapper;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonVocabularyRepository;
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private static final String VOCABULARY_ALREADY_IN_LESSON_MESSAGE = "Từ vựng đã được gắn vào Lesson này";

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;
    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarRepository grammarRepository;
    private final GrammarExampleRepository grammarExampleRepository;
    private final GrammarMapper grammarMapper;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

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
    public LessonResponse getPublishedLessonById(Long id, Long currentUserId) {
        Lesson lesson = findLessonOrThrow(id);
        if (lesson.getStatus() != LessonStatus.PUBLISHED || lesson.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            // Không tiết lộ Lesson/Course DRAFT tồn tại - trả cùng lỗi với id không tồn tại.
            throw new ResourceNotFoundException();
        }
        boolean enrolled = currentUserId != null
                && courseEnrollmentRepository.existsByUserIdAndCourseId(currentUserId, lesson.getCourse().getId());
        return enrolled ? toLessonResponse(lesson, true) : toPreviewLessonResponse(lesson);
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
        // Admin luôn thấy đầy đủ - không gating theo Enroll (dùng endpoint riêng, không qua getPublishedLessonById).
        return toLessonResponse(findLessonOrThrow(id), true);
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
        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toResponse(saved, true, List.of(), List.of());
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
        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved, true);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = findLessonOrThrow(id);
        lesson.setDeleted(true);
        lesson.setDeletedAt(LocalDateTime.now());
        lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void attachVocabularyToLesson(Long lessonId, LessonVocabularyAttachRequest request) {
        Lesson lesson = findLessonOrThrow(lessonId);
        Vocabulary vocabulary =
                vocabularyRepository.findById(request.getVocabularyId()).orElseThrow(ResourceNotFoundException::new);
        if (lessonVocabularyRepository.existsByLessonIdAndVocabularyId(lessonId, request.getVocabularyId())) {
            throw new DuplicateResourceException(VOCABULARY_ALREADY_IN_LESSON_MESSAGE);
        }

        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        lessonVocabulary.setLesson(lesson);
        lessonVocabulary.setVocabulary(vocabulary);
        lessonVocabulary.setDisplayOrder(request.getDisplayOrder());
        lessonVocabularyRepository.save(lessonVocabulary);
    }

    @Override
    @Transactional
    public void detachVocabularyFromLesson(Long lessonId, Long vocabularyId) {
        LessonVocabulary lessonVocabulary = lessonVocabularyRepository
                .findByLessonIdAndVocabularyId(lessonId, vocabularyId)
                .orElseThrow(ResourceNotFoundException::new);
        lessonVocabularyRepository.delete(lessonVocabulary);
    }

    private Lesson findLessonOrThrow(Long id) {
        return lessonRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    /** enrolled=false, không nhúng Vocabulary/Grammar - chỉ preview field gốc của Lesson. */
    private LessonResponse toPreviewLessonResponse(Lesson lesson) {
        return lessonMapper.toResponse(lesson, false, List.of(), List.of());
    }

    private LessonResponse toLessonResponse(Lesson lesson, boolean enrolled) {
        List<LessonVocabularyResponse> vocabularies = lessonVocabularyRepository
                .findAllByLessonIdOrderByDisplayOrderAsc(lesson.getId())
                .stream()
                .map(this::toLessonVocabularyResponse)
                .toList();
        List<GrammarResponse> grammars = grammarRepository
                .findAllByLessonIdOrderByDisplayOrderAsc(lesson.getId())
                .stream()
                .map(grammar -> grammarMapper.toResponse(
                        grammar,
                        grammarMapper.toExampleResponseList(grammarExampleRepository.findAllByGrammarId(grammar.getId()))))
                .toList();
        return lessonMapper.toResponse(lesson, enrolled, vocabularies, grammars);
    }

    private LessonVocabularyResponse toLessonVocabularyResponse(LessonVocabulary lessonVocabulary) {
        Vocabulary vocabulary = lessonVocabulary.getVocabulary();
        return LessonVocabularyResponse.builder()
                .vocabularyId(vocabulary.getId())
                .word(vocabulary.getWord())
                .meaning(vocabulary.getMeaning())
                .ipa(vocabulary.getIpa())
                .imageUrl(vocabulary.getImageUrl())
                .wordType(vocabulary.getWordType())
                .displayOrder(lessonVocabulary.getDisplayOrder())
                .build();
    }
}
