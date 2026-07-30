package com.languagelearning.language_learning_backend.review.service.impl;

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
import com.languagelearning.language_learning_backend.review.service.ReviewService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SM-2 rút gọn (docs/testing/04_BUSINESS_RULES_GLOBAL.md mục 3) — các hằng số/công thức cụ
 * thể bên dưới là quyết định chốt khi code (FRS chỉ cho bảng tác động định tính, không cho số
 * chính xác), miễn đảm bảo bất biến: interval sau EASY > GOOD > HARD cùng điều kiện ban đầu
 * (TC-SRS-009), FORGOT luôn reset repetitionCount/interval (TC-SRS-005).
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final float DEFAULT_EASE_FACTOR = 2.5f;
    private static final float MIN_EASE_FACTOR = 1.3f;
    private static final int FORGOT_INTERVAL_DAYS = 1;
    private static final int FIRST_SUCCESS_INTERVAL_DAYS = 1;
    private static final int SECOND_SUCCESS_INTERVAL_DAYS = 6;

    private final UserVocabularyProgressRepository userVocabularyProgressRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewTodayItemResponse> getTodayReviews(Long userId) {
        LocalDate today = todayForUser(userId);
        return userVocabularyProgressRepository
                .findAllByUserIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(userId, today)
                .stream()
                .map(this::toTodayItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewSubmitResponse submitReview(Long vocabularyId, ReviewSubmitRequest request, Long userId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(ResourceNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        LocalDate today = LocalDate.now(ZoneId.of(user.getTimezone()));

        UserVocabularyProgress progress = userVocabularyProgressRepository
                .findByUserIdAndVocabularyId(userId, vocabularyId)
                .orElseGet(() -> createInitialProgress(user, vocabulary));

        applyRating(progress, request.getRating(), today);
        progress = userVocabularyProgressRepository.save(progress);

        ReviewLog log = new ReviewLog();
        log.setUser(user);
        log.setVocabulary(vocabulary);
        log.setRating(request.getRating());
        log.setReviewedAt(LocalDateTime.now());
        reviewLogRepository.save(log);

        // XP (reason=REVIEW_DONE) + UserDailyActivity đợi hạ tầng D8/XpLog ở Giai đoạn 7,
        // giống Lesson complete (Giai đoạn 3) và Quiz submit (Giai đoạn 4).

        return toSubmitResponse(progress);
    }

    private LocalDate todayForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        return LocalDate.now(ZoneId.of(user.getTimezone()));
    }

    private UserVocabularyProgress createInitialProgress(User user, Vocabulary vocabulary) {
        UserVocabularyProgress progress = new UserVocabularyProgress();
        progress.setUser(user);
        progress.setVocabulary(vocabulary);
        progress.setEaseFactor(DEFAULT_EASE_FACTOR);
        progress.setIntervalDays(0);
        progress.setRepetitionCount(0);
        progress.setForgotCount(0);
        progress.setMasteryLevel(MasteryLevel.NEW);
        return progress;
    }

    private void applyRating(UserVocabularyProgress progress, ReviewRating rating, LocalDate today) {
        switch (rating) {
            case FORGOT -> {
                progress.setRepetitionCount(0);
                progress.setEaseFactor(Math.max(MIN_EASE_FACTOR, progress.getEaseFactor() - 0.2f));
                progress.setIntervalDays(FORGOT_INTERVAL_DAYS);
                progress.setForgotCount(progress.getForgotCount() + 1);
            }
            case HARD -> applySuccessfulReview(progress, -0.15f, 0.8);
            case GOOD -> applySuccessfulReview(progress, 0f, 1.0);
            case EASY -> applySuccessfulReview(progress, 0.15f, 1.3);
        }
        progress.setLastReviewDate(today);
        progress.setNextReviewDate(today.plusDays(progress.getIntervalDays()));
        progress.setMasteryLevel(computeMasteryLevel(progress.getRepetitionCount()));
    }

    /** intervalMultiplier: HARD tăng chậm hơn (0.8), GOOD chuẩn (1.0), EASY tăng nhanh hơn (1.3) — cùng baseInterval nên đảm bảo EASY > GOOD > HARD (TC-SRS-009). */
    private void applySuccessfulReview(UserVocabularyProgress progress, float easeFactorDelta, double intervalMultiplier) {
        int oldRepetitionCount = progress.getRepetitionCount();
        int oldIntervalDays = progress.getIntervalDays();
        float newEaseFactor = Math.max(MIN_EASE_FACTOR, progress.getEaseFactor() + easeFactorDelta);

        int baseIntervalDays;
        if (oldRepetitionCount == 0) {
            baseIntervalDays = FIRST_SUCCESS_INTERVAL_DAYS;
        } else if (oldRepetitionCount == 1) {
            baseIntervalDays = SECOND_SUCCESS_INTERVAL_DAYS;
        } else {
            baseIntervalDays = Math.round(oldIntervalDays * newEaseFactor);
        }

        progress.setRepetitionCount(oldRepetitionCount + 1);
        progress.setEaseFactor(newEaseFactor);
        progress.setIntervalDays(Math.max(1, Math.round((float) (baseIntervalDays * intervalMultiplier))));
    }

    private MasteryLevel computeMasteryLevel(int repetitionCount) {
        if (repetitionCount >= 6) {
            return MasteryLevel.MASTERED;
        }
        if (repetitionCount >= 3) {
            return MasteryLevel.FAMILIAR;
        }
        if (repetitionCount >= 1) {
            return MasteryLevel.LEARNING;
        }
        return MasteryLevel.NEW;
    }

    private ReviewTodayItemResponse toTodayItemResponse(UserVocabularyProgress progress) {
        Vocabulary vocabulary = progress.getVocabulary();
        return ReviewTodayItemResponse.builder()
                .vocabularyId(vocabulary.getId())
                .word(vocabulary.getWord())
                .meaning(vocabulary.getMeaning())
                .ipa(vocabulary.getIpa())
                .imageUrl(vocabulary.getImageUrl())
                .wordType(vocabulary.getWordType())
                .nextReviewDate(progress.getNextReviewDate())
                .masteryLevel(progress.getMasteryLevel())
                .build();
    }

    private ReviewSubmitResponse toSubmitResponse(UserVocabularyProgress progress) {
        return ReviewSubmitResponse.builder()
                // Dùng association (progress.getVocabulary().getId()), KHÔNG dùng field shadow
                // vocabularyId - field đó insertable=false/updatable=false nên chỉ được Hibernate
                // điền khi entity load từ 1 SELECT thật; với progress vừa new+save() (lần review
                // đầu tiên), shadow field vẫn null dù association đã có giá trị đúng.
                .vocabularyId(progress.getVocabulary().getId())
                .repetitionCount(progress.getRepetitionCount())
                .easeFactor(progress.getEaseFactor())
                .intervalDays(progress.getIntervalDays())
                .nextReviewDate(progress.getNextReviewDate())
                .lastReviewDate(progress.getLastReviewDate())
                .forgotCount(progress.getForgotCount())
                .masteryLevel(progress.getMasteryLevel())
                .build();
    }
}
