package com.languagelearning.language_learning_backend.search.service.impl;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.course.repository.CourseSpecification;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckSpecification;
import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarSpecification;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonSpecification;
import com.languagelearning.language_learning_backend.search.dto.response.SearchResponse;
import com.languagelearning.language_learning_backend.search.dto.response.SearchResultItem;
import com.languagelearning.language_learning_backend.search.enums.SearchResultType;
import com.languagelearning.language_learning_backend.search.service.SearchService;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularySpecification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    /** Số kết quả tối đa mỗi loại nội dung khi tìm kiếm gộp (không truyền `type`) - quyết định chốt khi code, FRS không cho số cụ thể. */
    private static final int GROUPED_RESULT_LIMIT = 5;

    private static final SearchResponse EMPTY_RESPONSE = SearchResponse.builder()
            .courses(List.of())
            .lessons(List.of())
            .vocabularies(List.of())
            .grammars(List.of())
            .decks(List.of())
            .build();

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarRepository grammarRepository;
    private final DeckRepository deckRepository;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String keyword, SearchResultType type, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return EMPTY_RESPONSE;
        }

        if (type != null) {
            return searchSingleType(keyword, type, pageable);
        }
        return searchGrouped(keyword);
    }

    private SearchResponse searchSingleType(String keyword, SearchResultType type, Pageable pageable) {
        SearchResponse.SearchResponseBuilder builder = SearchResponse.builder()
                .courses(List.of())
                .lessons(List.of())
                .vocabularies(List.of())
                .grammars(List.of())
                .decks(List.of());

        Page<SearchResultItem> page = switch (type) {
            case COURSE -> courseRepository.findAll(coursePublishedSpec(keyword), pageable).map(this::toItem);
            case LESSON -> lessonRepository.findAll(lessonPublishedSpec(keyword), pageable).map(this::toItem);
            case VOCABULARY -> vocabularyRepository.findAll(vocabularySearchableSpec(keyword), pageable).map(this::toItem);
            case GRAMMAR -> grammarRepository.findAll(grammarPublishedSpec(keyword), pageable).map(this::toItem);
            case DECK -> deckRepository.findAll(deckPublicSpec(keyword), pageable).map(this::toItem);
        };

        switch (type) {
            case COURSE -> builder.courses(page.getContent());
            case LESSON -> builder.lessons(page.getContent());
            case VOCABULARY -> builder.vocabularies(page.getContent());
            case GRAMMAR -> builder.grammars(page.getContent());
            case DECK -> builder.decks(page.getContent());
        }

        return builder.page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private SearchResponse searchGrouped(String keyword) {
        Pageable limit = PageRequest.of(0, GROUPED_RESULT_LIMIT);
        List<SearchResultItem> courses =
                courseRepository.findAll(coursePublishedSpec(keyword), limit).map(this::toItem).getContent();
        List<SearchResultItem> lessons =
                lessonRepository.findAll(lessonPublishedSpec(keyword), limit).map(this::toItem).getContent();
        List<SearchResultItem> vocabularies =
                vocabularyRepository.findAll(vocabularySearchableSpec(keyword), limit).map(this::toItem).getContent();
        List<SearchResultItem> grammars =
                grammarRepository.findAll(grammarPublishedSpec(keyword), limit).map(this::toItem).getContent();
        List<SearchResultItem> decks =
                deckRepository.findAll(deckPublicSpec(keyword), limit).map(this::toItem).getContent();

        return SearchResponse.builder()
                .courses(courses)
                .lessons(lessons)
                .vocabularies(vocabularies)
                .grammars(grammars)
                .decks(decks)
                .build();
    }

    private Specification<Course> coursePublishedSpec(String keyword) {
        return Specification.allOf(CourseSpecification.hasStatus(CourseStatus.PUBLISHED), CourseSpecification.titleContains(keyword));
    }

    private Specification<Lesson> lessonPublishedSpec(String keyword) {
        return Specification.allOf(
                LessonSpecification.hasStatus(LessonStatus.PUBLISHED),
                LessonSpecification.courseHasStatus(CourseStatus.PUBLISHED),
                LessonSpecification.titleContains(keyword));
    }

    private Specification<Vocabulary> vocabularySearchableSpec(String keyword) {
        return Specification.allOf(
                VocabularySpecification.hasStatus(VocabularyStatus.ACTIVE),
                VocabularySpecification.isSystemWord(),
                VocabularySpecification.wordOrMeaningContains(keyword));
    }

    private Specification<Grammar> grammarPublishedSpec(String keyword) {
        return Specification.allOf(
                GrammarSpecification.lessonHasStatus(LessonStatus.PUBLISHED),
                GrammarSpecification.lessonCourseHasStatus(CourseStatus.PUBLISHED),
                GrammarSpecification.titleOrPatternContains(keyword));
    }

    private Specification<Deck> deckPublicSpec(String keyword) {
        return Specification.allOf(
                DeckSpecification.hasVisibility(DeckVisibility.PUBLIC),
                DeckSpecification.hasStatus(DeckStatus.ACTIVE),
                DeckSpecification.titleContains(keyword));
    }

    private SearchResultItem toItem(Course course) {
        return SearchResultItem.builder()
                .type(SearchResultType.COURSE)
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getDifficulty())
                .imageUrl(course.getThumbnailUrl())
                .build();
    }

    private SearchResultItem toItem(Lesson lesson) {
        return SearchResultItem.builder()
                .type(SearchResultType.LESSON)
                .id(lesson.getId())
                .title(lesson.getTitle())
                .subtitle(lesson.getCourse().getTitle())
                .build();
    }

    private SearchResultItem toItem(Vocabulary vocabulary) {
        return SearchResultItem.builder()
                .type(SearchResultType.VOCABULARY)
                .id(vocabulary.getId())
                .title(vocabulary.getWord())
                .subtitle(vocabulary.getMeaning())
                .imageUrl(vocabulary.getImageUrl())
                .build();
    }

    private SearchResultItem toItem(Grammar grammar) {
        return SearchResultItem.builder()
                .type(SearchResultType.GRAMMAR)
                .id(grammar.getId())
                .title(grammar.getTitle())
                .subtitle(grammar.getPattern())
                .build();
    }

    private SearchResultItem toItem(Deck deck) {
        return SearchResultItem.builder()
                .type(SearchResultType.DECK)
                .id(deck.getId())
                .title(deck.getTitle())
                .imageUrl(deck.getCoverImageUrl())
                .build();
    }
}
