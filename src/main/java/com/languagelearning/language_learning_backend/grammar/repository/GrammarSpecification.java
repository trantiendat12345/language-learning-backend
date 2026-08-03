package com.languagelearning.language_learning_backend.grammar.repository;

import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filter động cho GET /api/search (Grammar) — theo đúng mẫu đặt tên ở docs/PROJECT_OVERVIEW.md
 * mục 7.1. Grammar không có field status riêng (theo ERD) — hiển thị phụ thuộc hoàn toàn vào
 * Lesson cha (và Course của Lesson đó) đều PUBLISHED, xem Javadoc Grammar entity.
 */
public final class GrammarSpecification {

    private GrammarSpecification() {
    }

    public static Specification<Grammar> lessonHasStatus(LessonStatus status) {
        return (root, query, cb) -> cb.equal(root.get("lesson").get("status"), status);
    }

    public static Specification<Grammar> lessonCourseHasStatus(CourseStatus status) {
        return (root, query, cb) -> cb.equal(root.get("lesson").get("course").get("status"), status);
    }

    public static Specification<Grammar> titleOrPatternContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("pattern")), pattern));
        };
    }
}
