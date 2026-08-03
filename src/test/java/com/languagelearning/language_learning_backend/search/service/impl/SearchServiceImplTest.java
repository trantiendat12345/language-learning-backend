package com.languagelearning.language_learning_backend.search.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.search.dto.response.SearchResponse;
import com.languagelearning.language_learning_backend.search.enums.SearchResultType;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private GrammarRepository grammarRepository;

    @Mock
    private DeckRepository deckRepository;

    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(
                courseRepository, lessonRepository, vocabularyRepository, grammarRepository, deckRepository);
    }

    private Course course(Long id, String title) {
        Course course = new Course();
        course.setId(id);
        course.setTitle(title);
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    private Deck deck(Long id, String title) {
        Deck deck = new Deck();
        deck.setId(id);
        deck.setTitle(title);
        return deck;
    }

    private Vocabulary vocabulary(Long id, String word) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(id);
        vocabulary.setWord(word);
        vocabulary.setMeaning("nghĩa");
        return vocabulary;
    }

    @Test
    void search_blankKeyword_returnsControlledEmptyResponseWithoutQuerying() {
        SearchResponse response = searchService.search("   ", null, PageRequest.of(0, 20));

        assertThat(response.getCourses()).isEmpty();
        assertThat(response.getLessons()).isEmpty();
        assertThat(response.getVocabularies()).isEmpty();
        assertThat(response.getGrammars()).isEmpty();
        assertThat(response.getDecks()).isEmpty();
        verifyNoInteractions(courseRepository, lessonRepository, vocabularyRepository, grammarRepository, deckRepository);
    }

    @Test
    void search_nullKeyword_returnsControlledEmptyResponse() {
        SearchResponse response = searchService.search(null, SearchResultType.COURSE, PageRequest.of(0, 20));

        assertThat(response.getCourses()).isEmpty();
        verifyNoInteractions(courseRepository);
    }

    @Test
    void search_withTypeCourse_onlyQueriesCourseRepositoryAndPopulatesPagination() {
        Pageable pageable = PageRequest.of(0, 20);
        when(courseRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.eq(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(course(1L, "English A1")), pageable, 1));

        SearchResponse response = searchService.search("english", SearchResultType.COURSE, pageable);

        assertThat(response.getCourses()).hasSize(1);
        assertThat(response.getCourses().get(0).getType()).isEqualTo(SearchResultType.COURSE);
        assertThat(response.getCourses().get(0).getTitle()).isEqualTo("English A1");
        assertThat(response.getLessons()).isEmpty();
        assertThat(response.getVocabularies()).isEmpty();
        assertThat(response.getGrammars()).isEmpty();
        assertThat(response.getDecks()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(lessonRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        verifyNoInteractions(lessonRepository, vocabularyRepository, grammarRepository, deckRepository);
    }

    @Test
    void search_withTypeDeck_onlyQueriesDeckRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        when(deckRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.eq(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(deck(2L, "TOEIC 600")), pageable, 1));

        SearchResponse response = searchService.search("toeic", SearchResultType.DECK, pageable);

        assertThat(response.getDecks()).hasSize(1);
        assertThat(response.getDecks().get(0).getTitle()).isEqualTo("TOEIC 600");
        verifyNoInteractions(courseRepository, lessonRepository, vocabularyRepository, grammarRepository);
    }

    @Test
    void search_withoutType_queriesAllFiveRepositoriesWithLimitedPageSize() {
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(course(1L, "English A1"))));
        when(lessonRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));
        when(vocabularyRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(vocabulary(3L, "apple"))));
        when(grammarRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));
        when(deckRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));

        SearchResponse response = searchService.search("a", null, PageRequest.of(0, 20));

        assertThat(response.getCourses()).hasSize(1);
        assertThat(response.getVocabularies()).hasSize(1);
        assertThat(response.getLessons()).isEmpty();
        assertThat(response.getGrammars()).isEmpty();
        assertThat(response.getDecks()).isEmpty();
        assertThat(response.getPage()).isNull();
        assertThat(response.getTotalElements()).isNull();
    }
}
