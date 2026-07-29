package com.languagelearning.language_learning_backend.course.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseSummaryResponse;
import com.languagelearning.language_learning_backend.course.service.CourseService;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint public - permitAll trong SecurityConfig (chỉ GET). */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final LessonService lessonService;

    @GetMapping
    public ApiResponse<PageResponse<CourseSummaryResponse>> getPublishedCourses(
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(courseService.getPublishedCourses(languageId, level, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        return ApiResponse.success(courseService.getPublishedCourseById(id));
    }

    @GetMapping("/{courseId}/lessons")
    public ApiResponse<List<LessonSummaryResponse>> getLessonsByCourse(@PathVariable Long courseId) {
        return ApiResponse.success(lessonService.getPublishedLessonsByCourse(courseId));
    }
}
