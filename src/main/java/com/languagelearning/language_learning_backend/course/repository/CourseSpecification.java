package com.languagelearning.language_learning_backend.course.repository;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Xây filter động cho GET /api/courses?languageId=&level=&keyword=&page= — theo đúng mẫu
 * "VocabularySpecification" đã định trước ở docs/PROJECT_OVERVIEW.md mục 7.1, tránh viết
 * @Query cố định không mở rộng được khi thêm filter mới.
 */
public final class CourseSpecification {

    private CourseSpecification() {
    }

    public static Specification<Course> hasStatus(CourseStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Course> hasLanguageId(Long languageId) {
        return (root, query, cb) -> languageId == null ? null : cb.equal(root.get("language").get("id"), languageId);
    }

    public static Specification<Course> hasDifficulty(String difficulty) {
        return (root, query, cb) -> difficulty == null || difficulty.isBlank()
                ? null
                : cb.equal(root.get("difficulty"), difficulty);
    }

    public static Specification<Course> titleContains(String keyword) {
        return (root, query, cb) -> keyword == null || keyword.isBlank()
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }
}
