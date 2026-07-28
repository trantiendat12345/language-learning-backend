package com.languagelearning.language_learning_backend.language.repository;

import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    boolean existsByCode(String code);

    List<Language> findAllByStatus(LanguageStatus status);
}
