package com.languagelearning.language_learning_backend.lesson.repository;

import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByCourseIdOrderByDisplayOrderAsc(Long courseId);

    List<Lesson> findAllByCourseIdAndStatusOrderByDisplayOrderAsc(Long courseId, LessonStatus status);
}
