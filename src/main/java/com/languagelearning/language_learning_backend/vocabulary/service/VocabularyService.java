package com.languagelearning.language_learning_backend.vocabulary.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyCreateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyUpdateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularySummaryResponse;
import org.springframework.data.domain.Pageable;

public interface VocabularyService {

    /** Chỉ trả từ hệ thống (owner null) status=ACTIVE, hỗ trợ filter languageId/keyword. */
    PageResponse<VocabularySummaryResponse> getSystemVocabularies(Long languageId, String keyword, Pageable pageable);

    /** 404 nếu không tồn tại, không phải từ hệ thống, hoặc không ACTIVE. */
    VocabularyResponse getSystemVocabularyById(Long id);

    PageResponse<VocabularySummaryResponse> getAllVocabulariesForAdmin(Pageable pageable);

    VocabularyResponse getVocabularyByIdForAdmin(Long id);

    /** Luôn tạo từ hệ thống (owner = null) - custom word của User thuộc luồng Deck ở Giai đoạn 5. */
    VocabularyResponse createVocabulary(VocabularyCreateRequest request);

    VocabularyResponse updateVocabulary(Long id, VocabularyUpdateRequest request);

    void deleteVocabulary(Long id);
}
