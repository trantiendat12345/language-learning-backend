package com.languagelearning.language_learning_backend.review.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.review.dto.request.ReviewSubmitRequest;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewSubmitResponse;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewTodayItemResponse;
import com.languagelearning.language_learning_backend.review.entity.ReviewLog;
import com.languagelearning.language_learning_backend.review.entity.UserVocabularyProgress;
import com.languagelearning.language_learning_backend.review.enums.MasteryLevel;
import com.languagelearning.language_learning_backend.review.enums.ReviewRating;
import com.languagelearning.language_learning_backend.review.repository.ReviewLogRepository;
import com.languagelearning.language_learning_backend.review.repository.UserVocabularyProgressRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private UserVocabularyProgressRepository userVocabularyProgressRepository;

    @Mock
    private ReviewLogRepository reviewLogRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private UserRepository userRepository;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(
                userVocabularyProgressRepository, reviewLogRepository, vocabularyRepository, userRepository);
    }

    private User user() {
        User user = new User();
        user.setId(100L);
        user.setTimezone("UTC");
        return user;
    }

    private Vocabulary vocabulary() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(50L);
        vocabulary.setWord("family");
        vocabulary.setMeaning("gia đình");
        return vocabulary;
    }

    private UserVocabularyProgress progress(int repetitionCount, int intervalDays, float easeFactor) {
        UserVocabularyProgress progress = new UserVocabularyProgress();
        progress.setId(1L);
        progress.setUser(user());
        progress.setVocabulary(vocabulary());
        progress.setRepetitionCount(repetitionCount);
        progress.setIntervalDays(intervalDays);
        progress.setEaseFactor(easeFactor);
        progress.setForgotCount(0);
        progress.setMasteryLevel(MasteryLevel.LEARNING);
        return progress;
    }

    private ReviewSubmitRequest request(ReviewRating rating) {
        ReviewSubmitRequest request = new ReviewSubmitRequest();
        request.setRating(rating);
        return request;
    }

    @Test
    void getTodayReviews_returnsOverdueItemsOrderedByNextReviewDate() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        UserVocabularyProgress progress = progress(2, 6, 2.5f);
        progress.setNextReviewDate(LocalDate.now());
        when(userVocabularyProgressRepository.findAllByUserIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
                        any(), any()))
                .thenReturn(List.of(progress));

        List<ReviewTodayItemResponse> result = reviewService.getTodayReviews(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWord()).isEqualTo("family");
    }

    @Test
    void submitReview_firstTimeGood_createsProgressWithDefaultsAndInterval1Day() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L)).thenReturn(Optional.empty());
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSubmitResponse response = reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L);

        assertThat(response.getVocabularyId()).isEqualTo(50L);
        assertThat(response.getRepetitionCount()).isEqualTo(1);
        assertThat(response.getEaseFactor()).isEqualTo(2.5f);
        assertThat(response.getIntervalDays()).isEqualTo(1);
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now(java.time.ZoneId.of("UTC")).plusDays(1));
        assertThat(response.getMasteryLevel()).isEqualTo(MasteryLevel.LEARNING);
        verify(reviewLogRepository).save(any(ReviewLog.class));
    }

    @Test
    void submitReview_forgot_resetsRepetitionAndInterval_incrementsForgotCount() {
        UserVocabularyProgress existing = progress(3, 15, 2.5f);
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L)).thenReturn(Optional.of(existing));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSubmitResponse response = reviewService.submitReview(50L, request(ReviewRating.FORGOT), 100L);

        assertThat(response.getRepetitionCount()).isZero();
        assertThat(response.getIntervalDays()).isEqualTo(1);
        assertThat(response.getForgotCount()).isEqualTo(1);
        assertThat(response.getEaseFactor()).isEqualTo(2.3f, org.assertj.core.data.Offset.offset(0.001f));
    }

    @Test
    void submitReview_forgot_easeFactorNeverGoesBelowFloor() {
        UserVocabularyProgress existing = progress(3, 15, 1.35f);
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L)).thenReturn(Optional.of(existing));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSubmitResponse response = reviewService.submitReview(50L, request(ReviewRating.FORGOT), 100L);

        assertThat(response.getEaseFactor()).isGreaterThanOrEqualTo(1.3f);
    }

    @Test
    void submitReview_easyVsGoodVsHard_intervalStrictlyIncreasing() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        int hardInterval = reviewService.submitReview(50L, request(ReviewRating.HARD), 100L).getIntervalDays();

        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        int goodInterval = reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L).getIntervalDays();

        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        int easyInterval = reviewService.submitReview(50L, request(ReviewRating.EASY), 100L).getIntervalDays();

        assertThat(easyInterval).isGreaterThan(goodInterval);
        assertThat(goodInterval).isGreaterThan(hardInterval);
    }

    @Test
    void submitReview_nextReviewDate_equalsTodayPlusInterval() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSubmitResponse response = reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L);

        LocalDate today = LocalDate.now(java.time.ZoneId.of("UTC"));
        assertThat(response.getNextReviewDate()).isEqualTo(today.plusDays(response.getIntervalDays()));
        assertThat(response.getLastReviewDate()).isEqualTo(today);
    }

    @Test
    void submitReview_masteryLevelProgressesWithRepetitionCount() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        MasteryLevel afterThird = reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L).getMasteryLevel();
        assertThat(afterThird).isEqualTo(MasteryLevel.FAMILIAR);

        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(5, 10, 2.5f)));
        MasteryLevel afterSixth = reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L).getMasteryLevel();
        assertThat(afterSixth).isEqualTo(MasteryLevel.MASTERED);
    }

    @Test
    void submitReview_whenVocabularyNotFound_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.submitReview(50L, request(ReviewRating.GOOD), 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitReview_alwaysSavesReviewLog() {
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user()));
        when(userVocabularyProgressRepository.findByUserIdAndVocabularyId(100L, 50L))
                .thenReturn(Optional.of(progress(2, 10, 2.5f)));
        when(userVocabularyProgressRepository.save(any(UserVocabularyProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.submitReview(50L, request(ReviewRating.EASY), 100L);

        verify(reviewLogRepository).save(any(ReviewLog.class));
    }
}
