package com.languagelearning.language_learning_backend.quiz.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionCreateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionOptionRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionUpdateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionOptionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionResponse;
import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import com.languagelearning.language_learning_backend.quiz.mapper.QuestionMapper;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionOptionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionRepository;
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
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        QuestionMapper mapper = Mappers.getMapper(QuestionMapper.class);
        questionService = new QuestionServiceImpl(
                questionRepository, questionOptionRepository, languageRepository, vocabularyRepository, mapper);
    }

    private Language language() {
        Language language = new Language();
        language.setId(1L);
        return language;
    }

    private Question question() {
        Question question = new Question();
        question.setId(1L);
        question.setLanguage(language());
        question.setSourceType(QuestionSourceType.LESSON);
        question.setSourceId(10L);
        question.setType(QuestionType.MULTIPLE_CHOICE);
        question.setPromptText("Apple nghĩa là gì?");
        return question;
    }

    private QuestionOptionRequest optionRequest(String text, boolean correct) {
        QuestionOptionRequest request = new QuestionOptionRequest();
        request.setOptionText(text);
        request.setCorrect(correct);
        return request;
    }

    private QuestionCreateRequest createRequest(QuestionType type, List<QuestionOptionRequest> options) {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setLanguageId(1L);
        request.setType(type);
        request.setPromptText("Apple nghĩa là gì?");
        request.setOptions(options);
        return request;
    }

    @Test
    void createQuestion_withExactlyOneCorrectOption_savesQuestionAndOptions() {
        QuestionCreateRequest request = createRequest(
                QuestionType.MULTIPLE_CHOICE,
                List.of(optionRequest("quả táo", true), optionRequest("quả cam", false)));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question question = invocation.getArgument(0);
            question.setId(1L);
            return question;
        });
        when(questionOptionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = questionService.createQuestion(request);

        assertThat(response.getPromptText()).isEqualTo("Apple nghĩa là gì?");
        assertThat(response.getOptions()).hasSize(2);
        assertThat(response.getOptions()).filteredOn(QuestionOptionResponse::isCorrect).hasSize(1);
    }

    @Test
    void createQuestion_multipleChoiceWithZeroCorrectOptions_throwsBadRequestException() {
        QuestionCreateRequest request = createRequest(
                QuestionType.MULTIPLE_CHOICE,
                List.of(optionRequest("quả táo", false), optionRequest("quả cam", false)));

        assertThatThrownBy(() -> questionService.createQuestion(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void createQuestion_multipleChoiceWithTwoCorrectOptions_throwsBadRequestException() {
        QuestionCreateRequest request = createRequest(
                QuestionType.MULTIPLE_CHOICE,
                List.of(optionRequest("quả táo", true), optionRequest("quả cam", true)));

        assertThatThrownBy(() -> questionService.createQuestion(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void createQuestion_listeningType_skipsCorrectOptionValidation() {
        QuestionCreateRequest request = createRequest(QuestionType.LISTENING, List.of());
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question question = invocation.getArgument(0);
            question.setId(1L);
            return question;
        });

        QuestionResponse response = questionService.createQuestion(request);

        assertThat(response.getType()).isEqualTo(QuestionType.LISTENING);
    }

    @Test
    void createQuestion_whenLanguageNotFound_throwsResourceNotFoundException() {
        QuestionCreateRequest request =
                createRequest(QuestionType.MULTIPLE_CHOICE, List.of(optionRequest("a", true)));
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.createQuestion(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getQuestionByIdForAdmin_whenFound_returnsResponseWithOptions() {
        Question question = question();
        QuestionOption option = new QuestionOption();
        option.setId(5L);
        option.setOptionText("quả táo");
        option.setCorrect(true);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionOptionRepository.findAllByQuestionId(1L)).thenReturn(List.of(option));

        QuestionResponse response = questionService.getQuestionByIdForAdmin(1L);

        assertThat(response.getOptions()).hasSize(1);
        assertThat(response.getOptions().get(0).isCorrect()).isTrue();
    }

    @Test
    void getQuestionByIdForAdmin_whenNotFound_throwsResourceNotFoundException() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getQuestionByIdForAdmin(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateQuestion_whenFound_replacesOptions() {
        Question question = question();
        QuestionOption oldOption = new QuestionOption();
        oldOption.setId(5L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionOptionRepository.findAllByQuestionId(1L)).thenReturn(List.of(oldOption));
        when(questionOptionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setLanguageId(1L);
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setPromptText("Updated prompt");
        request.setOptions(List.of(optionRequest("new option", true)));

        QuestionResponse response = questionService.updateQuestion(1L, request);

        assertThat(response.getPromptText()).isEqualTo("Updated prompt");
        assertThat(oldOption.isDeleted()).isTrue();
    }

    @Test
    void deleteQuestion_whenFound_softDeletesQuestionAndOptions() {
        Question question = question();
        QuestionOption option = new QuestionOption();
        option.setId(5L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionOptionRepository.findAllByQuestionId(1L)).thenReturn(List.of(option));
        when(questionOptionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        questionService.deleteQuestion(1L);

        assertThat(question.isDeleted()).isTrue();
        assertThat(option.isDeleted()).isTrue();
    }

    @Test
    void deleteQuestion_whenNotFound_throwsResourceNotFoundException() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.deleteQuestion(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
