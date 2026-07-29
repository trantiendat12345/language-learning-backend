package com.languagelearning.language_learning_backend.lesson.service;

import com.languagelearning.language_learning_backend.lesson.dto.request.LessonCreateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonUpdateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonVocabularyAttachRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import java.util.List;

public interface LessonService {

    /** 404 nếu Course không tồn tại/không PUBLISHED; chỉ trả Lesson status=PUBLISHED. */
    List<LessonSummaryResponse> getPublishedLessonsByCourse(Long courseId);

    /**
     * 404 nếu Lesson hoặc Course cha không tồn tại/không PUBLISHED. `currentUserId` null =
     * chưa đăng nhập (luôn preview). Đã enroll Course chứa Lesson → trả đầy đủ
     * Vocabulary/Grammar; chưa enroll → preview (2 field đó rỗng, `enrolled=false`).
     */
    LessonResponse getPublishedLessonById(Long id, Long currentUserId);

    List<LessonSummaryResponse> getLessonsByCourseForAdmin(Long courseId);

    LessonResponse getLessonByIdForAdmin(Long id);

    LessonResponse createLesson(Long courseId, LessonCreateRequest request);

    LessonResponse updateLesson(Long id, LessonUpdateRequest request);

    void deleteLesson(Long id);

    /** 409 nếu từ đã gắn vào Lesson này (unique lessonId+vocabularyId). */
    void attachVocabularyToLesson(Long lessonId, LessonVocabularyAttachRequest request);

    void detachVocabularyFromLesson(Long lessonId, Long vocabularyId);
}
