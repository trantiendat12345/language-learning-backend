package com.languagelearning.language_learning_backend.quiz.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.BadRequestException;

/** Ném ra khi POST /api/quizzes/attempts có questionId không thuộc sourceType/sourceId đã khai báo (chặn gian lận). */
public class QuizAnswerOutOfScopeException extends BadRequestException {

    public QuizAnswerOutOfScopeException() {
        super(ErrorCode.QUIZ_ANSWER_OUT_OF_SCOPE, ErrorMessage.QUIZ_ANSWER_OUT_OF_SCOPE);
    }
}
