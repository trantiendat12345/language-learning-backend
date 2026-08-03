package com.languagelearning.language_learning_backend.admin.service.impl;

import com.languagelearning.language_learning_backend.admin.dto.response.AdminDashboardResponse;
import com.languagelearning.language_learning_backend.admin.service.AdminDashboardService;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.quiz.repository.QuizAttemptRepository;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final DeckRepository deckRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .totalCourses(courseRepository.count())
                .totalLessons(lessonRepository.count())
                .totalVocabulary(vocabularyRepository.count())
                .totalDecks(deckRepository.count())
                .totalQuizAttempts(quizAttemptRepository.count())
                .build();
    }
}
