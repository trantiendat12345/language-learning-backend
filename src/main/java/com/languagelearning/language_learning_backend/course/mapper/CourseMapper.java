package com.languagelearning.language_learning_backend.course.mapper;

import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseSummaryResponse;
import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "languageId", source = "course.language.id")
    @Mapping(target = "languageCode", source = "course.language.code")
    @Mapping(target = "languageName", source = "course.language.name")
    @Mapping(target = "lessons", source = "lessons")
    CourseResponse toResponse(Course course, List<LessonSummaryResponse> lessons);

    @Mapping(target = "languageCode", source = "language.code")
    CourseSummaryResponse toSummaryResponse(Course course);
}
