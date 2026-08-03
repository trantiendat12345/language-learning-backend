package com.languagelearning.language_learning_backend.lesson.repository;

import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import org.springframework.data.jpa.domain.Specification;

/** Filter động cho GET /api/search (Lesson) — theo đúng mẫu đặt tên ở docs/PROJECT_OVERVIEW.md mục 7.1. */
public final class LessonSpecification {

    private LessonSpecification() {
    }

    public static Specification<Lesson> hasStatus(LessonStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Course cha cũng phải PUBLISHED - Lesson không tự đủ điều kiện hiển thị nếu Course cha chưa/không còn PUBLISHED. */
    public static Specification<Lesson> courseHasStatus(CourseStatus status) {
        return (root, query, cb) -> cb.equal(root.get("course").get("status"), status);
    }

    public static Specification<Lesson> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }
}
