package com.languagelearning.language_learning_backend.history.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.history.dto.response.ActivityHistoryResponse;
import com.languagelearning.language_learning_backend.history.entity.ActivityHistory;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
import com.languagelearning.language_learning_backend.history.repository.ActivityHistoryRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ActivityHistoryServiceImplTest {

    @Mock
    private ActivityHistoryRepository activityHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    private ActivityHistoryServiceImpl activityHistoryService;

    @BeforeEach
    void setUp() {
        activityHistoryService = new ActivityHistoryServiceImpl(
                activityHistoryRepository, userRepository, courseRepository, lessonRepository, deckRepository, vocabularyRepository);
    }

    @Test
    void recordActivity_savesNewRowEveryCall_notIdempotent() {
        User user = new User();
        user.setId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        activityHistoryService.recordActivity(100L, ActivityTargetType.COURSE, 1L, ActivityAction.VIEWED);
        activityHistoryService.recordActivity(100L, ActivityTargetType.COURSE, 1L, ActivityAction.VIEWED);

        ArgumentCaptor<ActivityHistory> captor = ArgumentCaptor.forClass(ActivityHistory.class);
        verify(activityHistoryRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getValue().getTargetType()).isEqualTo(ActivityTargetType.COURSE);
        assertThat(captor.getValue().getTargetId()).isEqualTo(1L);
        assertThat(captor.getValue().getAction()).isEqualTo(ActivityAction.VIEWED);
        assertThat(captor.getValue().getOccurredAt()).isNotNull();
    }

    @Test
    void getMyHistory_noActionFilter_resolvesTitleFromCourse() {
        ActivityHistory history = new ActivityHistory();
        history.setId(1L);
        history.setTargetType(ActivityTargetType.COURSE);
        history.setTargetId(1L);
        history.setAction(ActivityAction.VIEWED);
        history.setOccurredAt(LocalDateTime.now());
        when(activityHistoryRepository.findAllByUserIdOrderByOccurredAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(List.of(history));
        Course course = new Course();
        course.setId(1L);
        course.setTitle("English A1");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        List<ActivityHistoryResponse> result = activityHistoryService.getMyHistory(100L, null, 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("English A1");
    }

    @Test
    void getMyHistory_withActionFilter_usesFilteredQuery() {
        when(activityHistoryRepository.findAllByUserIdAndActionOrderByOccurredAtDesc(
                        eq(100L), eq(ActivityAction.REVIEWED), any(Pageable.class)))
                .thenReturn(List.of());

        List<ActivityHistoryResponse> result = activityHistoryService.getMyHistory(100L, ActivityAction.REVIEWED, 50);

        assertThat(result).isEmpty();
        verify(activityHistoryRepository).findAllByUserIdAndActionOrderByOccurredAtDesc(
                eq(100L), eq(ActivityAction.REVIEWED), any(Pageable.class));
    }

    @Test
    void getMyHistory_targetDeleted_titleNullButRowStillShown() {
        ActivityHistory history = new ActivityHistory();
        history.setId(1L);
        history.setTargetType(ActivityTargetType.VOCABULARY);
        history.setTargetId(999L);
        history.setAction(ActivityAction.REVIEWED);
        history.setOccurredAt(LocalDateTime.now());
        when(activityHistoryRepository.findAllByUserIdOrderByOccurredAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(List.of(history));
        when(vocabularyRepository.findById(999L)).thenReturn(Optional.empty());

        List<ActivityHistoryResponse> result = activityHistoryService.getMyHistory(100L, null, 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isNull();
    }

    @Test
    void getMyHistory_vocabularyTarget_resolvesWordAsTitle() {
        ActivityHistory history = new ActivityHistory();
        history.setId(1L);
        history.setTargetType(ActivityTargetType.VOCABULARY);
        history.setTargetId(5L);
        history.setAction(ActivityAction.REVIEWED);
        history.setOccurredAt(LocalDateTime.now());
        when(activityHistoryRepository.findAllByUserIdOrderByOccurredAtDesc(eq(100L), any(Pageable.class)))
                .thenReturn(List.of(history));
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(5L);
        vocabulary.setWord("apple");
        when(vocabularyRepository.findById(5L)).thenReturn(Optional.of(vocabulary));

        List<ActivityHistoryResponse> result = activityHistoryService.getMyHistory(100L, null, 50);

        assertThat(result.get(0).getTitle()).isEqualTo("apple");
    }
}
