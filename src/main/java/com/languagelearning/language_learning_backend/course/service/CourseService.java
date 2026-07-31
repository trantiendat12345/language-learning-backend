package com.languagelearning.language_learning_backend.course.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.course.dto.request.CourseCreateRequest;
import com.languagelearning.language_learning_backend.course.dto.request.CourseUpdateRequest;
import com.languagelearning.language_learning_backend.course.dto.response.CourseResponse;
import com.languagelearning.language_learning_backend.course.dto.response.CourseSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    /** Chỉ trả Course status=PUBLISHED, hỗ trợ filter languageId/difficulty/keyword. */
    PageResponse<CourseSummaryResponse> getPublishedCourses(
            Long languageId, String difficulty, String keyword, Pageable pageable);

    /**
     * 404 nếu không tồn tại HOẶC không PUBLISHED — không tiết lộ Course DRAFT tồn tại.
     * currentUserId nullable (route public) — ghi ActivityHistory(VIEWED) nếu đã đăng nhập.
     */
    CourseResponse getPublishedCourseById(Long id, Long currentUserId);

    PageResponse<CourseSummaryResponse> getAllCoursesForAdmin(Pageable pageable);

    CourseResponse getCourseByIdForAdmin(Long id);

    CourseResponse createCourse(CourseCreateRequest request);

    CourseResponse updateCourse(Long id, CourseUpdateRequest request);

    void deleteCourse(Long id);
}
