package com.languagelearning.language_learning_backend.vocabulary.repository;

import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import org.springframework.data.jpa.domain.Specification;

/** Filter động cho GET /api/vocabularies?languageId=&keyword=&page= — theo đúng mẫu đặt tên ở docs/PROJECT_OVERVIEW.md mục 7.1. */
public final class VocabularySpecification {

    private VocabularySpecification() {
    }

    public static Specification<Vocabulary> hasStatus(VocabularyStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Chỉ từ hệ thống (owner null) — public không thấy từ custom của người khác. */
    public static Specification<Vocabulary> isSystemWord() {
        return (root, query, cb) -> cb.isNull(root.get("owner"));
    }

    public static Specification<Vocabulary> hasLanguageId(Long languageId) {
        return (root, query, cb) -> languageId == null ? null : cb.equal(root.get("language").get("id"), languageId);
    }

    public static Specification<Vocabulary> wordOrMeaningContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("word")), pattern), cb.like(cb.lower(root.get("meaning")), pattern));
        };
    }
}
