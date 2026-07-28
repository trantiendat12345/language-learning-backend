package com.languagelearning.language_learning_backend.language.service;

import com.languagelearning.language_learning_backend.language.dto.request.LanguageCreateRequest;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageUpdateRequest;
import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import java.util.List;

public interface LanguageService {

    /** Chỉ trả Language status=ACTIVE — dùng cho API public GET /api/languages. */
    List<LanguageResponse> getActiveLanguages();

    /** Trả toàn bộ Language bất kể status — dùng cho Admin. */
    List<LanguageResponse> getAllLanguagesForAdmin();

    LanguageResponse getLanguageByIdForAdmin(Long id);

    LanguageResponse createLanguage(LanguageCreateRequest request);

    LanguageResponse updateLanguage(Long id, LanguageUpdateRequest request);

    void deleteLanguage(Long id);
}
