package com.languagelearning.language_learning_backend.vocabulary.mapper;

import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularySummaryResponse;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VocabularyMapper {

    @Mapping(target = "languageId", source = "language.id")
    @Mapping(target = "languageCode", source = "language.code")
    @Mapping(target = "languageName", source = "language.name")
    VocabularyResponse toResponse(Vocabulary vocabulary);

    @Mapping(target = "languageCode", source = "language.code")
    VocabularySummaryResponse toSummaryResponse(Vocabulary vocabulary);
}
