package com.languagelearning.language_learning_backend.review.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.review.enums.ReviewRating;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewSubmitRequest {

    @NotNull(message = ValidationMessage.REVIEW_RATING_REQUIRED)
    private ReviewRating rating;
}
