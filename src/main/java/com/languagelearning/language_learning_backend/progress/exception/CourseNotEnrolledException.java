package com.languagelearning.language_learning_backend.progress.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.BusinessException;
import org.springframework.http.HttpStatus;

/** Ném ra khi User gọi POST /api/lessons/{id}/complete cho Lesson thuộc Course chưa enroll. */
public class CourseNotEnrolledException extends BusinessException {

    public CourseNotEnrolledException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.COURSE_NOT_ENROLLED, ErrorMessage.COURSE_NOT_ENROLLED);
    }
}
