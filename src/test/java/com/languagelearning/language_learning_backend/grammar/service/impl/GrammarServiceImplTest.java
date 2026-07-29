package com.languagelearning.language_learning_backend.grammar.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarCreateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarExampleRequest;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarUpdateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarSummaryResponse;
import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import com.languagelearning.language_learning_backend.grammar.entity.GrammarExample;
import com.languagelearning.language_learning_backend.grammar.mapper.GrammarMapper;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarExampleRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrammarServiceImplTest {

    @Mock
    private GrammarRepository grammarRepository;

    @Mock
    private GrammarExampleRepository grammarExampleRepository;

    @Mock
    private LessonRepository lessonRepository;

    private GrammarServiceImpl grammarService;

    @BeforeEach
    void setUp() {
        GrammarMapper grammarMapper = Mappers.getMapper(GrammarMapper.class);
        grammarService = new GrammarServiceImpl(grammarRepository, grammarExampleRepository, lessonRepository, grammarMapper);
    }

    private Lesson lesson() {
        Lesson lesson = new Lesson();
        lesson.setId(10L);
        return lesson;
    }

    private Grammar grammar() {
        Grammar grammar = new Grammar();
        grammar.setId(1L);
        grammar.setLesson(lesson());
        grammar.setTitle("Simple Present — to be");
        grammar.setPattern("S + am/is/are + ...");
        grammar.setDisplayOrder(1);
        return grammar;
    }

    @Test
    void getGrammarsByLessonForAdmin_whenLessonExists_returnsMappedList() {
        when(lessonRepository.existsById(10L)).thenReturn(true);
        when(grammarRepository.findAllByLessonIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(grammar()));

        List<GrammarSummaryResponse> result = grammarService.getGrammarsByLessonForAdmin(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Simple Present — to be");
    }

    @Test
    void getGrammarsByLessonForAdmin_whenLessonNotFound_throwsResourceNotFoundException() {
        when(lessonRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> grammarService.getGrammarsByLessonForAdmin(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getGrammarByIdForAdmin_whenFound_returnsResponseWithExamples() {
        Grammar grammar = grammar();
        GrammarExample example = new GrammarExample();
        example.setId(5L);
        example.setExampleText("I am a student.");
        when(grammarRepository.findById(1L)).thenReturn(Optional.of(grammar));
        when(grammarExampleRepository.findAllByGrammarId(1L)).thenReturn(List.of(example));

        GrammarResponse response = grammarService.getGrammarByIdForAdmin(1L);

        assertThat(response.getTitle()).isEqualTo("Simple Present — to be");
        assertThat(response.getLessonId()).isEqualTo(10L);
        assertThat(response.getExamples()).hasSize(1);
        assertThat(response.getExamples().get(0).getExampleText()).isEqualTo("I am a student.");
    }

    @Test
    void getGrammarByIdForAdmin_whenNotFound_throwsResourceNotFoundException() {
        when(grammarRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarService.getGrammarByIdForAdmin(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private GrammarCreateRequest createRequest() {
        GrammarCreateRequest request = new GrammarCreateRequest();
        request.setTitle("Simple Present — to be");
        request.setPattern("S + am/is/are + ...");
        GrammarExampleRequest example = new GrammarExampleRequest();
        example.setExampleText("I am a student.");
        example.setTranslation("Tôi là học sinh.");
        request.setExamples(List.of(example));
        return request;
    }

    @Test
    void createGrammar_whenLessonExists_savesGrammarAndExamples() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson()));
        when(grammarRepository.save(any(Grammar.class))).thenAnswer(invocation -> {
            Grammar grammar = invocation.getArgument(0);
            grammar.setId(1L);
            return grammar;
        });
        when(grammarExampleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GrammarResponse response = grammarService.createGrammar(10L, createRequest());

        assertThat(response.getTitle()).isEqualTo("Simple Present — to be");
        assertThat(response.getExamples()).hasSize(1);
        assertThat(response.getExamples().get(0).getExampleText()).isEqualTo("I am a student.");
    }

    @Test
    void createGrammar_whenLessonNotFound_throwsResourceNotFoundException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarService.createGrammar(10L, createRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateGrammar_whenFound_replacesExamples() {
        Grammar grammar = grammar();
        GrammarExample oldExample = new GrammarExample();
        oldExample.setId(5L);
        when(grammarRepository.findById(1L)).thenReturn(Optional.of(grammar));
        when(grammarRepository.save(any(Grammar.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(grammarExampleRepository.findAllByGrammarId(1L)).thenReturn(List.of(oldExample));
        when(grammarExampleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GrammarUpdateRequest request = new GrammarUpdateRequest();
        request.setTitle("Updated Grammar");
        request.setDisplayOrder(2);
        GrammarExampleRequest newExample = new GrammarExampleRequest();
        newExample.setExampleText("She is a teacher.");
        request.setExamples(List.of(newExample));

        GrammarResponse response = grammarService.updateGrammar(1L, request);

        assertThat(response.getTitle()).isEqualTo("Updated Grammar");
        assertThat(oldExample.isDeleted()).isTrue();
        assertThat(response.getExamples()).hasSize(1);
        assertThat(response.getExamples().get(0).getExampleText()).isEqualTo("She is a teacher.");
    }

    @Test
    void updateGrammar_whenNotFound_throwsResourceNotFoundException() {
        when(grammarRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarService.updateGrammar(1L, new GrammarUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteGrammar_whenFound_softDeletesGrammarAndExamples() {
        Grammar grammar = grammar();
        GrammarExample example = new GrammarExample();
        example.setId(5L);
        when(grammarRepository.findById(1L)).thenReturn(Optional.of(grammar));
        when(grammarRepository.save(any(Grammar.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(grammarExampleRepository.findAllByGrammarId(1L)).thenReturn(List.of(example));
        when(grammarExampleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        grammarService.deleteGrammar(1L);

        assertThat(grammar.isDeleted()).isTrue();
        assertThat(grammar.getDeletedAt()).isNotNull();
        assertThat(example.isDeleted()).isTrue();
    }

    @Test
    void deleteGrammar_whenNotFound_throwsResourceNotFoundException() {
        when(grammarRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarService.deleteGrammar(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
