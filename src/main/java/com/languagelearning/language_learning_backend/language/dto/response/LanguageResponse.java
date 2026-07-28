package com.languagelearning.language_learning_backend.language.dto.response;

import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageResponse {

    private Long id;
    private String code;
    private String name;
    private String flagIconUrl;
    private LanguageStatus status;
}
