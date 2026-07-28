package com.languagelearning.language_learning_backend.language.mapper;

import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import com.languagelearning.language_learning_backend.language.entity.Language;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    LanguageResponse toResponse(Language language);
}
