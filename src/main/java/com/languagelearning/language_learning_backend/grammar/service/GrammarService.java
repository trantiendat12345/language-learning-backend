package com.languagelearning.language_learning_backend.grammar.service;

import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarCreateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarUpdateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarSummaryResponse;
import java.util.List;

public interface GrammarService {

    /** 404 nếu Lesson không tồn tại. */
    List<GrammarSummaryResponse> getGrammarsByLessonForAdmin(Long lessonId);

    GrammarResponse getGrammarByIdForAdmin(Long id);

    GrammarResponse createGrammar(Long lessonId, GrammarCreateRequest request);

    GrammarResponse updateGrammar(Long id, GrammarUpdateRequest request);

    void deleteGrammar(Long id);
}
