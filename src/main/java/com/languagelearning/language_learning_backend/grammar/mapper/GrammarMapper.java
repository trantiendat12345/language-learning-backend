package com.languagelearning.language_learning_backend.grammar.mapper;

import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarExampleResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarSummaryResponse;
import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import com.languagelearning.language_learning_backend.grammar.entity.GrammarExample;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GrammarMapper {

    @Mapping(target = "lessonId", source = "grammar.lesson.id")
    @Mapping(target = "examples", source = "examples")
    GrammarResponse toResponse(Grammar grammar, List<GrammarExampleResponse> examples);

    GrammarSummaryResponse toSummaryResponse(Grammar grammar);

    GrammarExampleResponse toExampleResponse(GrammarExample example);

    List<GrammarExampleResponse> toExampleResponseList(List<GrammarExample> examples);
}
