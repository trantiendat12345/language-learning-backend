package com.languagelearning.language_learning_backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.admin.dto.response.AdminDashboardResponse;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    private AdminDashboardServiceImpl adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardServiceImpl(
                userRepository, courseRepository, lessonRepository, vocabularyRepository, deckRepository, quizAttemptRepository);
    }

    @Test
    void getDashboard_returnsCountsFromEachRepository() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(7L);
        when(courseRepository.count()).thenReturn(5L);
        when(lessonRepository.count()).thenReturn(20L);
        when(vocabularyRepository.count()).thenReturn(300L);
        when(deckRepository.count()).thenReturn(15L);
        when(quizAttemptRepository.count()).thenReturn(42L);

        AdminDashboardResponse response = adminDashboardService.getDashboard();

        assertThat(response.getTotalUsers()).isEqualTo(10L);
        assertThat(response.getActiveUsers()).isEqualTo(7L);
        assertThat(response.getTotalCourses()).isEqualTo(5L);
        assertThat(response.getTotalLessons()).isEqualTo(20L);
        assertThat(response.getTotalVocabulary()).isEqualTo(300L);
        assertThat(response.getTotalDecks()).isEqualTo(15L);
        assertThat(response.getTotalQuizAttempts()).isEqualTo(42L);
    }
}
