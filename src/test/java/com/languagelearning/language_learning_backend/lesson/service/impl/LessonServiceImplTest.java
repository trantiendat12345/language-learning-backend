package com.languagelearning.language_learning_backend.lesson.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.grammar.mapper.GrammarMapper;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarExampleRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonCreateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonUpdateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonVocabularyAttachRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.entity.LessonVocabulary;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.mapper.LessonMapper;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonVocabularyRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
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

    @Mock
    private LessonVocabularyRepository lessonVocabularyRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private GrammarRepository grammarRepository;

    @Mock
    private GrammarExampleRepository grammarExampleRepository;

    private LessonServiceImpl lessonService;

    @BeforeEach
    void setUp() {
        LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);
        GrammarMapper grammarMapper = Mappers.getMapper(GrammarMapper.class);
        lessonService = new LessonServiceImpl(
                lessonRepository,
                courseRepository,
                lessonMapper,
                lessonVocabularyRepository,
                vocabularyRepository,
                grammarRepository,
                grammarExampleRepository,
                grammarMapper);
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

    private Vocabulary vocabulary() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(100L);
        Language language = new Language();
        language.setId(1L);
        vocabulary.setLanguage(language);
        vocabulary.setWord("family");
        vocabulary.setMeaning("gia đình");
        return vocabulary;
    }

    /** Stub 2 query nội dung nhúng (LessonVocabulary + Grammar) rỗng - dùng chung cho các test không quan tâm nội dung nhúng. */
    private void stubEmbeddedContentEmpty(Long lessonId) {
        when(lessonVocabularyRepository.findAllByLessonIdOrderByDisplayOrderAsc(lessonId)).thenReturn(List.of());
        when(grammarRepository.findAllByLessonIdOrderByDisplayOrderAsc(lessonId)).thenReturn(List.of());
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
    void getPublishedLessonById_whenLessonAndCoursePublished_returnsResponseWithEmbeddedContent() {
        Course course = publishedCourse();
        Lesson lesson = publishedLesson(course);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        lessonVocabulary.setVocabulary(vocabulary());
        lessonVocabulary.setDisplayOrder(1);
        when(lessonVocabularyRepository.findAllByLessonIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(lessonVocabulary));
        when(grammarRepository.findAllByLessonIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of());

        LessonResponse response = lessonService.getPublishedLessonById(10L);

        assertThat(response.getTitle()).isEqualTo("Lesson 1");
        assertThat(response.getCourseId()).isEqualTo(1L);
        assertThat(response.getVocabularies()).hasSize(1);
        assertThat(response.getVocabularies().get(0).getWord()).isEqualTo("family");
        assertThat(response.getGrammars()).isEmpty();
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
        assertThat(response.getVocabularies()).isEmpty();
        assertThat(response.getGrammars()).isEmpty();
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
        stubEmbeddedContentEmpty(10L);

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

    @Test
    void attachVocabularyToLesson_whenNotYetAttached_saves() {
        Lesson lesson = publishedLesson(publishedCourse());
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(vocabularyRepository.findById(100L)).thenReturn(Optional.of(vocabulary()));
        when(lessonVocabularyRepository.existsByLessonIdAndVocabularyId(10L, 100L)).thenReturn(false);

        LessonVocabularyAttachRequest request = new LessonVocabularyAttachRequest();
        request.setVocabularyId(100L);
        request.setDisplayOrder(1);

        lessonService.attachVocabularyToLesson(10L, request);

        verify(lessonVocabularyRepository).save(any(LessonVocabulary.class));
    }

    @Test
    void attachVocabularyToLesson_whenAlreadyAttached_throwsDuplicateResourceException() {
        Lesson lesson = publishedLesson(publishedCourse());
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(vocabularyRepository.findById(100L)).thenReturn(Optional.of(vocabulary()));
        when(lessonVocabularyRepository.existsByLessonIdAndVocabularyId(10L, 100L)).thenReturn(true);

        LessonVocabularyAttachRequest request = new LessonVocabularyAttachRequest();
        request.setVocabularyId(100L);

        assertThatThrownBy(() -> lessonService.attachVocabularyToLesson(10L, request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(lessonVocabularyRepository, never()).save(any());
    }

    @Test
    void attachVocabularyToLesson_whenVocabularyNotFound_throwsResourceNotFoundException() {
        Lesson lesson = publishedLesson(publishedCourse());
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(vocabularyRepository.findById(100L)).thenReturn(Optional.empty());

        LessonVocabularyAttachRequest request = new LessonVocabularyAttachRequest();
        request.setVocabularyId(100L);

        assertThatThrownBy(() -> lessonService.attachVocabularyToLesson(10L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void detachVocabularyFromLesson_whenFound_deletes() {
        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        lessonVocabulary.setId(1L);
        when(lessonVocabularyRepository.findByLessonIdAndVocabularyId(10L, 100L)).thenReturn(Optional.of(lessonVocabulary));

        lessonService.detachVocabularyFromLesson(10L, 100L);

        verify(lessonVocabularyRepository).delete(lessonVocabulary);
    }

    @Test
    void detachVocabularyFromLesson_whenNotFound_throwsResourceNotFoundException() {
        when(lessonVocabularyRepository.findByLessonIdAndVocabularyId(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.detachVocabularyFromLesson(10L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
