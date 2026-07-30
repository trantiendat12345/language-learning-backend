package com.languagelearning.language_learning_backend.quiz.service.impl;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionCreateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionOptionRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionUpdateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import com.languagelearning.language_learning_backend.quiz.mapper.QuestionMapper;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionOptionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionRepository;
import com.languagelearning.language_learning_backend.quiz.service.QuestionService;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final String EXACTLY_ONE_CORRECT_OPTION_MESSAGE =
            "Loại câu hỏi này phải có đúng 1 option correct=true";

    /** Type có duy nhất 1 đáp án đúng để so khớp (khác MATCHING/REORDER cần nhiều cặp/thứ tự) - xem QuestionType. */
    private static final Set<QuestionType> SINGLE_CORRECT_OPTION_TYPES = EnumSet.of(
            QuestionType.MULTIPLE_CHOICE,
            QuestionType.FILL_BLANK,
            QuestionType.TYPING,
            QuestionType.IMAGE_CHOICE,
            QuestionType.AUDIO_CHOICE);

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final LanguageRepository languageRepository;
    private final VocabularyRepository vocabularyRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionSummaryResponse> getAllQuestionsForAdmin(Pageable pageable) {
        return PageResponse.from(questionRepository.findAll(pageable).map(questionMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionByIdForAdmin(Long id) {
        return toQuestionResponse(findQuestionOrThrow(id));
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionCreateRequest request) {
        validateExactlyOneCorrectOption(request.getType(), request.getOptions());
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);
        Vocabulary vocabulary = findVocabularyIfPresent(request.getVocabularyId());

        Question question = new Question();
        question.setSourceType(request.getSourceType());
        question.setSourceId(request.getSourceId());
        question.setLanguage(language);
        question.setType(request.getType());
        question.setVocabulary(vocabulary);
        question.setPromptText(request.getPromptText());
        question.setPromptAudioUrl(request.getPromptAudioUrl());
        question.setPromptImageUrl(request.getPromptImageUrl());
        question.setExplanation(request.getExplanation());
        question.setDifficulty(request.getDifficulty());
        question = questionRepository.save(question);

        List<QuestionOption> options = saveOptions(question, request.getOptions());
        return questionMapper.toResponse(question, questionMapper.toOptionResponseList(options));
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionUpdateRequest request) {
        validateExactlyOneCorrectOption(request.getType(), request.getOptions());
        Question question = findQuestionOrThrow(id);
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);
        Vocabulary vocabulary = findVocabularyIfPresent(request.getVocabularyId());

        question.setSourceType(request.getSourceType());
        question.setSourceId(request.getSourceId());
        question.setLanguage(language);
        question.setType(request.getType());
        question.setVocabulary(vocabulary);
        question.setPromptText(request.getPromptText());
        question.setPromptAudioUrl(request.getPromptAudioUrl());
        question.setPromptImageUrl(request.getPromptImageUrl());
        question.setExplanation(request.getExplanation());
        question.setDifficulty(request.getDifficulty());
        question = questionRepository.save(question);

        // Update = thay toàn bộ danh sách option (giống Grammar/GrammarExample).
        softDeleteOptions(questionOptionRepository.findAllByQuestionId(id));
        List<QuestionOption> options = saveOptions(question, request.getOptions());
        return questionMapper.toResponse(question, questionMapper.toOptionResponseList(options));
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = findQuestionOrThrow(id);
        question.setDeleted(true);
        question.setDeletedAt(LocalDateTime.now());
        questionRepository.save(question);
        softDeleteOptions(questionOptionRepository.findAllByQuestionId(id));
    }

    private void validateExactlyOneCorrectOption(QuestionType type, List<QuestionOptionRequest> options) {
        if (!SINGLE_CORRECT_OPTION_TYPES.contains(type)) {
            return;
        }
        long correctCount = options == null ? 0 : options.stream().filter(QuestionOptionRequest::isCorrect).count();
        if (correctCount != 1) {
            throw new BadRequestException(EXACTLY_ONE_CORRECT_OPTION_MESSAGE);
        }
    }

    private Vocabulary findVocabularyIfPresent(Long vocabularyId) {
        if (vocabularyId == null) {
            return null;
        }
        return vocabularyRepository.findById(vocabularyId).orElseThrow(ResourceNotFoundException::new);
    }

    private QuestionResponse toQuestionResponse(Question question) {
        List<QuestionOption> options = questionOptionRepository.findAllByQuestionId(question.getId());
        return questionMapper.toResponse(question, questionMapper.toOptionResponseList(options));
    }

    private List<QuestionOption> saveOptions(Question question, List<QuestionOptionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<QuestionOption> options = requests.stream()
                .map(request -> {
                    QuestionOption option = new QuestionOption();
                    option.setQuestion(question);
                    option.setOptionText(request.getOptionText());
                    option.setOptionImageUrl(request.getOptionImageUrl());
                    option.setCorrect(request.isCorrect());
                    option.setDisplayOrder(request.getDisplayOrder());
                    return option;
                })
                .toList();
        return questionOptionRepository.saveAll(options);
    }

    private void softDeleteOptions(List<QuestionOption> options) {
        LocalDateTime now = LocalDateTime.now();
        options.forEach(option -> {
            option.setDeleted(true);
            option.setDeletedAt(now);
        });
        questionOptionRepository.saveAll(options);
    }

    private Question findQuestionOrThrow(Long id) {
        return questionRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
