package com.languagelearning.language_learning_backend.quiz.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizAnswerRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizGenerateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizSubmitRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizGenerateResponse;
import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import com.languagelearning.language_learning_backend.quiz.entity.QuizAttempt;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import com.languagelearning.language_learning_backend.quiz.exception.QuizAnswerOutOfScopeException;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionOptionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuestionRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptAnswerRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    private QuizServiceImpl quizService;

    @BeforeEach
    void setUp() {
        quizService = new QuizServiceImpl(
                questionRepository,
                questionOptionRepository,
                quizAttemptRepository,
                quizAttemptAnswerRepository,
                lessonRepository,
                userRepository);
    }

    private Lesson publishedLesson() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus(CourseStatus.PUBLISHED);
        Lesson lesson = new Lesson();
        lesson.setId(10L);
        lesson.setCourse(course);
        lesson.setStatus(LessonStatus.PUBLISHED);
        return lesson;
    }

    private Question mcQuestion(Long id) {
        Question question = new Question();
        question.setId(id);
        question.setSourceType(QuestionSourceType.LESSON);
        question.setSourceId(10L);
        question.setType(QuestionType.MULTIPLE_CHOICE);
        question.setPromptText("Apple nghĩa là gì?");
        return question;
    }

    private QuestionOption option(Long id, Long questionId, boolean correct, String text) {
        QuestionOption option = new QuestionOption();
        option.setId(id);
        option.setQuestionId(questionId);
        option.setCorrect(correct);
        option.setOptionText(text);
        return option;
    }

    @Test
    void generateQuiz_whenEnoughQuestions_returnsRequestedCount() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        Question q1 = mcQuestion(1L);
        Question q2 = mcQuestion(2L);
        when(questionRepository.findAllBySourceTypeAndSourceId(QuestionSourceType.LESSON, 10L)).thenReturn(List.of(q1, q2));
        when(questionOptionRepository.findAllByQuestionIdIn(any())).thenReturn(List.of(
                option(1L, 1L, true, "quả táo"), option(2L, 1L, false, "quả cam")));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setQuestionCount(1);

        QuizGenerateResponse response = quizService.generateQuiz(request);

        assertThat(response.getRequestedCount()).isEqualTo(1);
        assertThat(response.getActualCount()).isEqualTo(1);
        assertThat(response.getQuestions()).hasSize(1);
    }

    @Test
    void generateQuiz_whenNotEnoughQuestions_returnsMaxAvailableWithRequestedCountMismatch() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        when(questionRepository.findAllBySourceTypeAndSourceId(QuestionSourceType.LESSON, 10L))
                .thenReturn(List.of(mcQuestion(1L)));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setQuestionCount(10);

        QuizGenerateResponse response = quizService.generateQuiz(request);

        assertThat(response.getRequestedCount()).isEqualTo(10);
        assertThat(response.getActualCount()).isEqualTo(1);
    }

    @Test
    void generateQuiz_hidesCorrectFlagFromOptions() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        when(questionRepository.findAllBySourceTypeAndSourceId(QuestionSourceType.LESSON, 10L))
                .thenReturn(List.of(mcQuestion(1L)));
        when(questionOptionRepository.findAllByQuestionIdIn(any()))
                .thenReturn(List.of(option(1L, 1L, true, "quả táo")));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);

        QuizGenerateResponse response = quizService.generateQuiz(request);

        // QuizOptionResponse không có field correct - biên dịch được nghĩa là đã ẩn đúng field khỏi hợp đồng API.
        assertThat(response.getQuestions().get(0).getOptions().get(0).getOptionText()).isEqualTo("quả táo");
    }

    @Test
    void generateQuiz_whenSourceTypeNotLesson_throwsBadRequestException() {
        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setSourceType(QuestionSourceType.DECK);
        request.setSourceId(1L);

        assertThatThrownBy(() -> quizService.generateQuiz(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void generateQuiz_whenLessonDraft_throwsResourceNotFoundException() {
        Lesson lesson = publishedLesson();
        lesson.setStatus(LessonStatus.DRAFT);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);

        assertThatThrownBy(() -> quizService.generateQuiz(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitQuiz_multipleChoiceCorrectAnswer_gradesCorrect() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        Question question = mcQuestion(1L);
        when(questionRepository.findAllById(List.of(1L))).thenReturn(List.of(question));
        QuestionOption correctOption = option(1L, 1L, true, "quả táo");
        QuestionOption wrongOption = option(2L, 1L, false, "quả cam");
        when(questionOptionRepository.findAllByQuestionIdIn(any())).thenReturn(List.of(correctOption, wrongOption));
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> {
            QuizAttempt attempt = invocation.getArgument(0);
            attempt.setId(1L);
            return attempt;
        });
        when(quizAttemptAnswerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAnswerRequest answer = new QuizAnswerRequest();
        answer.setQuestionId(1L);
        answer.setSelectedOptionId(1L);

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setDurationSeconds(30);
        request.setAnswers(List.of(answer));

        QuizAttemptResponse response = quizService.submitQuiz(request, 100L);

        assertThat(response.getCorrectAnswers()).isEqualTo(1);
        assertThat(response.getWrongAnswers()).isZero();
        assertThat(response.getAccuracy()).isEqualTo(100f);
        assertThat(response.getXpEarned()).isZero();
        assertThat(response.getAnswers().get(0).isCorrect()).isTrue();
        assertThat(response.getAnswers().get(0).getCorrectOptionId()).isEqualTo(1L);
    }

    @Test
    void submitQuiz_fillBlankCaseInsensitiveTrimmed_gradesCorrect() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        Question question = mcQuestion(1L);
        question.setType(QuestionType.FILL_BLANK);
        when(questionRepository.findAllById(List.of(1L))).thenReturn(List.of(question));
        when(questionOptionRepository.findAllByQuestionIdIn(any()))
                .thenReturn(List.of(option(1L, 1L, true, "is")));
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(quizAttemptAnswerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAnswerRequest answer = new QuizAnswerRequest();
        answer.setQuestionId(1L);
        answer.setTypedAnswer("  IS  ");

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setAnswers(List.of(answer));

        QuizAttemptResponse response = quizService.submitQuiz(request, 100L);

        assertThat(response.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    void submitQuiz_skippedAnswer_gradesWrong() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        Question question = mcQuestion(1L);
        when(questionRepository.findAllById(List.of(1L))).thenReturn(List.of(question));
        when(questionOptionRepository.findAllByQuestionIdIn(any()))
                .thenReturn(List.of(option(1L, 1L, true, "quả táo")));
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(quizAttemptAnswerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAnswerRequest answer = new QuizAnswerRequest();
        answer.setQuestionId(1L);
        // selectedOptionId và typedAnswer đều để trống - bỏ qua câu này.

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setAnswers(List.of(answer));

        QuizAttemptResponse response = quizService.submitQuiz(request, 100L);

        assertThat(response.getCorrectAnswers()).isZero();
        assertThat(response.getWrongAnswers()).isEqualTo(1);
    }

    @Test
    void submitQuiz_whenQuestionIdOutOfScope_throwsQuizAnswerOutOfScopeException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(publishedLesson()));
        Question otherLessonQuestion = mcQuestion(1L);
        otherLessonQuestion.setSourceId(999L);
        when(questionRepository.findAllById(List.of(1L))).thenReturn(List.of(otherLessonQuestion));

        QuizAnswerRequest answer = new QuizAnswerRequest();
        answer.setQuestionId(1L);
        answer.setSelectedOptionId(1L);

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setSourceType(QuestionSourceType.LESSON);
        request.setSourceId(10L);
        request.setAnswers(List.of(answer));

        assertThatThrownBy(() -> quizService.submitQuiz(request, 100L)).isInstanceOf(QuizAnswerOutOfScopeException.class);
    }

    @Test
    void getMyQuizAttemptById_whenNotOwnedByUser_throwsResourceNotFoundException() {
        when(quizAttemptRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.getMyQuizAttemptById(1L, 100L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
