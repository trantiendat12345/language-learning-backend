package com.languagelearning.language_learning_backend.quiz.service.impl;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizAnswerRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizGenerateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizSubmitRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptAnswerResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizGenerateResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizOptionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizQuestionResponse;
import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import com.languagelearning.language_learning_backend.quiz.entity.QuizAttempt;
import com.languagelearning.language_learning_backend.quiz.entity.QuizAttemptAnswer;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import com.languagelearning.language_learning_backend.quiz.exception.QuizAnswerOutOfScopeException;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionOptionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptAnswerRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.quiz.service.QuizService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private static final String UNSUPPORTED_SOURCE_TYPE_MESSAGE_FORMAT = "sourceType %s chưa được hỗ trợ";

    /** Type có option để chọn (khác FILL_BLANK/TYPING nhập tự do) — chỉ nhóm này mới trả options trong QuizQuestionResponse. */
    private static final Set<QuestionType> OPTION_BASED_TYPES =
            EnumSet.of(QuestionType.MULTIPLE_CHOICE, QuestionType.IMAGE_CHOICE, QuestionType.AUDIO_CHOICE);

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public QuizGenerateResponse generateQuiz(QuizGenerateRequest request) {
        validateSourceTypeSupported(request.getSourceType());
        validateLessonSource(request.getSourceId());

        List<Question> allQuestions =
                questionRepository.findAllBySourceTypeAndSourceId(request.getSourceType(), request.getSourceId());
        List<Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);

        int requestedCount = request.getQuestionCount() == null ? shuffled.size() : request.getQuestionCount();
        int actualCount = Math.min(requestedCount, shuffled.size());
        List<Question> selected = shuffled.subList(0, actualCount);

        Map<Long, List<QuestionOption>> optionsByQuestion = fetchOptionsByQuestionId(selected.stream()
                .map(Question::getId)
                .toList());

        List<QuizQuestionResponse> questions = selected.stream()
                .map(question -> toQuizQuestionResponse(question, optionsByQuestion.getOrDefault(question.getId(), List.of())))
                .toList();

        return QuizGenerateResponse.builder()
                .questions(questions)
                .requestedCount(requestedCount)
                .actualCount(actualCount)
                .build();
    }

    @Override
    @Transactional
    public QuizAttemptResponse submitQuiz(QuizSubmitRequest request, Long userId) {
        validateSourceTypeSupported(request.getSourceType());
        validateLessonSource(request.getSourceId());

        List<Long> questionIds =
                request.getAnswers().stream().map(QuizAnswerRequest::getQuestionId).toList();
        Map<Long, Question> questionsById =
                questionRepository.findAllById(questionIds).stream().collect(Collectors.toMap(Question::getId, q -> q));
        for (Long questionId : questionIds) {
            Question question = questionsById.get(questionId);
            if (question == null
                    || question.getSourceType() != request.getSourceType()
                    || !question.getSourceId().equals(request.getSourceId())) {
                throw new QuizAnswerOutOfScopeException();
            }
        }

        Map<Long, List<QuestionOption>> optionsByQuestion = fetchOptionsByQuestionId(questionIds);
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setSourceType(request.getSourceType());
        attempt.setSourceId(request.getSourceId());
        attempt.setDurationSeconds(request.getDurationSeconds());
        attempt.setCompletedAt(LocalDateTime.now());

        List<GradedAnswer> gradedAnswers = new ArrayList<>();
        int correctCount = 0;
        for (QuizAnswerRequest answerRequest : request.getAnswers()) {
            Question question = questionsById.get(answerRequest.getQuestionId());
            List<QuestionOption> options = optionsByQuestion.getOrDefault(question.getId(), List.of());
            QuestionOption correctOption =
                    options.stream().filter(QuestionOption::isCorrect).findFirst().orElse(null);
            QuestionOption selectedOption = options.stream()
                    .filter(option -> option.getId().equals(answerRequest.getSelectedOptionId()))
                    .findFirst()
                    .orElse(null);

            boolean correct = gradeAnswer(question.getType(), selectedOption, answerRequest.getTypedAnswer(), correctOption);
            if (correct) {
                correctCount++;
            }
            gradedAnswers.add(new GradedAnswer(question, selectedOption, answerRequest.getTypedAnswer(), correct));
        }

        int total = request.getAnswers().size();
        int wrong = total - correctCount;
        float accuracy = total == 0 ? 0f : correctCount * 100f / total;

        attempt.setTotalQuestions(total);
        attempt.setCorrectAnswers(correctCount);
        attempt.setWrongAnswers(wrong);
        attempt.setAccuracy(accuracy);
        // Chưa có công thức score riêng biệt với accuracy (vd trọng số theo difficulty) - để dành khi cần.
        attempt.setScore(accuracy);
        // XP thật (cộng User.xp + ghi XpLog cùng transaction - CLAUDE.md #9) đợi hạ tầng D8 ở Giai đoạn 7.
        attempt.setXpEarned(0);
        attempt = quizAttemptRepository.save(attempt);

        List<QuizAttemptAnswer> savedAnswers = saveAnswers(attempt, gradedAnswers);
        return toQuizAttemptResponse(attempt, savedAnswers, optionsByQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuizAttemptSummaryResponse> getMyQuizAttempts(Long userId, Pageable pageable) {
        return PageResponse.from(quizAttemptRepository
                .findAllByUserIdOrderByCompletedAtDesc(userId, pageable)
                .map(this::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptResponse getMyQuizAttemptById(Long id, Long userId) {
        QuizAttempt attempt =
                quizAttemptRepository.findByIdAndUserId(id, userId).orElseThrow(ResourceNotFoundException::new);
        List<QuizAttemptAnswer> answers = quizAttemptAnswerRepository.findAllByQuizAttemptIdOrderByDisplayOrderAsc(id);
        Map<Long, List<QuestionOption>> optionsByQuestion =
                fetchOptionsByQuestionId(answers.stream().map(a -> a.getQuestion().getId()).toList());
        return toQuizAttemptResponse(attempt, answers, optionsByQuestion);
    }

    private void validateSourceTypeSupported(QuestionSourceType sourceType) {
        if (sourceType != QuestionSourceType.LESSON) {
            throw new BadRequestException(String.format(UNSUPPORTED_SOURCE_TYPE_MESSAGE_FORMAT, sourceType));
        }
    }

    /** Chỉ hỗ trợ sourceType=LESSON ở chunk này - validate Lesson tồn tại + PUBLISHED (404 nếu không, không tiết lộ DRAFT). */
    private void validateLessonSource(Long sourceId) {
        Lesson lesson = lessonRepository.findById(sourceId).orElseThrow(ResourceNotFoundException::new);
        if (lesson.getStatus() != LessonStatus.PUBLISHED || lesson.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException();
        }
    }

    private Map<Long, List<QuestionOption>> fetchOptionsByQuestionId(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionOptionRepository.findAllByQuestionIdIn(questionIds).stream()
                .collect(Collectors.groupingBy(QuestionOption::getQuestionId));
    }

    private QuizQuestionResponse toQuizQuestionResponse(Question question, List<QuestionOption> options) {
        List<QuestionOption> shuffledOptions = new ArrayList<>(options);
        Collections.shuffle(shuffledOptions);
        List<QuizOptionResponse> optionResponses = OPTION_BASED_TYPES.contains(question.getType())
                ? shuffledOptions.stream()
                        .map(option -> QuizOptionResponse.builder()
                                .id(option.getId())
                                .optionText(option.getOptionText())
                                .optionImageUrl(option.getOptionImageUrl())
                                .build())
                        .toList()
                : List.of();

        return QuizQuestionResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .promptText(question.getPromptText())
                .promptAudioUrl(question.getPromptAudioUrl())
                .promptImageUrl(question.getPromptImageUrl())
                .options(optionResponses)
                .build();
    }

    /**
     * Chỉ so khớp đúng cho MULTIPLE_CHOICE/IMAGE_CHOICE/AUDIO_CHOICE (theo selectedOptionId) và
     * FILL_BLANK/TYPING (theo typedAnswer, không phân biệt hoa/thường, trim khoảng trắng - xem
     * docs/testing/04_BUSINESS_RULES_GLOBAL.md mục 5). LISTENING/MATCHING/REORDER chưa hỗ trợ
     * chấm điểm ở chunk này (Quiz nâng cao, Phase 2) - luôn tính sai nếu lỡ xuất hiện.
     */
    private boolean gradeAnswer(QuestionType type, QuestionOption selectedOption, String typedAnswer, QuestionOption correctOption) {
        if (correctOption == null) {
            return false;
        }
        return switch (type) {
            case MULTIPLE_CHOICE, IMAGE_CHOICE, AUDIO_CHOICE ->
                selectedOption != null && selectedOption.getId().equals(correctOption.getId());
            case FILL_BLANK, TYPING ->
                typedAnswer != null
                        && correctOption.getOptionText() != null
                        && typedAnswer.trim().equalsIgnoreCase(correctOption.getOptionText().trim());
            default -> false;
        };
    }

    private List<QuizAttemptAnswer> saveAnswers(QuizAttempt attempt, List<GradedAnswer> gradedAnswers) {
        List<QuizAttemptAnswer> answers = new ArrayList<>();
        int order = 0;
        for (GradedAnswer graded : gradedAnswers) {
            QuizAttemptAnswer answer = new QuizAttemptAnswer();
            answer.setQuizAttempt(attempt);
            answer.setQuestion(graded.question());
            answer.setSelectedOption(graded.selectedOption());
            answer.setTypedAnswer(graded.typedAnswer());
            answer.setCorrect(graded.correct());
            answer.setDisplayOrder(order++);
            answers.add(answer);
        }
        return quizAttemptAnswerRepository.saveAll(answers);
    }

    private QuizAttemptSummaryResponse toSummaryResponse(QuizAttempt attempt) {
        return QuizAttemptSummaryResponse.builder()
                .id(attempt.getId())
                .sourceType(attempt.getSourceType())
                .sourceId(attempt.getSourceId())
                .totalQuestions(attempt.getTotalQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .accuracy(attempt.getAccuracy())
                .completedAt(attempt.getCompletedAt())
                .build();
    }

    private QuizAttemptResponse toQuizAttemptResponse(
            QuizAttempt attempt, List<QuizAttemptAnswer> answers, Map<Long, List<QuestionOption>> optionsByQuestion) {
        List<QuizAttemptAnswerResponse> answerResponses = answers.stream()
                .map(answer -> toAnswerResponse(
                        answer, optionsByQuestion.getOrDefault(answer.getQuestion().getId(), List.of())))
                .toList();

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .sourceType(attempt.getSourceType())
                .sourceId(attempt.getSourceId())
                .totalQuestions(attempt.getTotalQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .wrongAnswers(attempt.getWrongAnswers())
                .score(attempt.getScore())
                .accuracy(attempt.getAccuracy())
                .durationSeconds(attempt.getDurationSeconds())
                .xpEarned(attempt.getXpEarned())
                .completedAt(attempt.getCompletedAt())
                .answers(answerResponses)
                .build();
    }

    private QuizAttemptAnswerResponse toAnswerResponse(QuizAttemptAnswer answer, List<QuestionOption> options) {
        Question question = answer.getQuestion();
        QuestionOption correctOption = options.stream().filter(QuestionOption::isCorrect).findFirst().orElse(null);
        QuestionOption selectedOption = answer.getSelectedOption();

        return QuizAttemptAnswerResponse.builder()
                .questionId(question.getId())
                .type(question.getType())
                .promptText(question.getPromptText())
                .explanation(question.getExplanation())
                .selectedOptionId(selectedOption == null ? null : selectedOption.getId())
                .typedAnswer(answer.getTypedAnswer())
                .correctOptionId(correctOption == null ? null : correctOption.getId())
                .correct(answer.isCorrect())
                .build();
    }

    private record GradedAnswer(Question question, QuestionOption selectedOption, String typedAnswer, boolean correct) {
    }
}
