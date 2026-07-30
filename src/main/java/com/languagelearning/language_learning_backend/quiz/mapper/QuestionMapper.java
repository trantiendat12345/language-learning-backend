package com.languagelearning.language_learning_backend.quiz.mapper;

import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionOptionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "languageId", source = "question.language.id")
    @Mapping(target = "vocabularyId", source = "question.vocabulary.id")
    @Mapping(target = "options", source = "options")
    QuestionResponse toResponse(Question question, List<QuestionOptionResponse> options);

    QuestionSummaryResponse toSummaryResponse(Question question);

    QuestionOptionResponse toOptionResponse(QuestionOption option);

    List<QuestionOptionResponse> toOptionResponseList(List<QuestionOption> options);
}
