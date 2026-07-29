package com.languagelearning.language_learning_backend.course.repository;

import com.languagelearning.language_learning_backend.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    boolean existsBySlug(String slug);
}
