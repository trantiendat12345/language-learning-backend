package com.languagelearning.language_learning_backend.progress.mapper;

import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseEnrollmentMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    CourseEnrollmentResponse toResponse(CourseEnrollment enrollment);
}
