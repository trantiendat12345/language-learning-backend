package com.languagelearning.language_learning_backend.course.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.course.dto.request.CourseCreateRequest;
import com.languagelearning.language_learning_backend.course.dto.request.CourseUpdateRequest;
import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseSummaryResponse;
import com.languagelearning.language_learning_backend.course.service.CourseService;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonCreateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quản trị Course + Lesson lồng trong Course - bắt buộc role ADMIN. */
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;
    private final LessonService lessonService;

    @GetMapping
    public ApiResponse<PageResponse<CourseSummaryResponse>> getAllCourses(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(courseService.getAllCoursesForAdmin(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        return ApiResponse.success(courseService.getCourseByIdForAdmin(id));
    }

    @PostMapping
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, courseService.createCourse(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @GetMapping("/{courseId}/lessons")
    public ApiResponse<List<LessonSummaryResponse>> getLessonsByCourse(@PathVariable Long courseId) {
        return ApiResponse.success(lessonService.getLessonsByCourseForAdmin(courseId));
    }

    @PostMapping("/{courseId}/lessons")
    public ApiResponse<LessonResponse> createLesson(
            @PathVariable Long courseId, @Valid @RequestBody LessonCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, lessonService.createLesson(courseId, request));
    }
}
