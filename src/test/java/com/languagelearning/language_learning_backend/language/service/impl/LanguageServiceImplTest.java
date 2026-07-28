package com.languagelearning.language_learning_backend.language.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageCreateRequest;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageUpdateRequest;
import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import com.languagelearning.language_learning_backend.language.mapper.LanguageMapper;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

    @Mock
    private LanguageRepository languageRepository;

    private LanguageServiceImpl languageService;

    @BeforeEach
    void setUp() {
        LanguageMapper languageMapper = Mappers.getMapper(LanguageMapper.class);
        languageService = new LanguageServiceImpl(languageRepository, languageMapper);
    }

    private Language activeLanguage() {
        Language language = new Language();
        language.setId(1L);
        language.setCode("en");
        language.setName("English");
        language.setStatus(LanguageStatus.ACTIVE);
        return language;
    }

    @Test
    void getActiveLanguages_returnsOnlyActiveMapped() {
        when(languageRepository.findAllByStatus(LanguageStatus.ACTIVE)).thenReturn(List.of(activeLanguage()));

        List<LanguageResponse> result = languageService.getActiveLanguages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("en");
    }

    @Test
    void getAllLanguagesForAdmin_returnsAllRegardlessOfStatus() {
        when(languageRepository.findAll()).thenReturn(List.of(activeLanguage()));

        List<LanguageResponse> result = languageService.getAllLanguagesForAdmin();

        assertThat(result).hasSize(1);
    }

    @Test
    void getLanguageByIdForAdmin_whenFound_returnsMapped() {
        when(languageRepository.findById(1L)).thenReturn(Optional.of(activeLanguage()));

        LanguageResponse response = languageService.getLanguageByIdForAdmin(1L);

        assertThat(response.getCode()).isEqualTo("en");
    }

    @Test
    void getLanguageByIdForAdmin_whenNotFound_throwsResourceNotFoundException() {
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getLanguageByIdForAdmin(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private LanguageCreateRequest createRequest() {
        LanguageCreateRequest request = new LanguageCreateRequest();
        request.setCode("ja");
        request.setName("Japanese");
        return request;
    }

    @Test
    void createLanguage_withNewCode_savesAndReturnsMapped() {
        LanguageCreateRequest request = createRequest();
        when(languageRepository.existsByCode("ja")).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
            Language language = invocation.getArgument(0);
            language.setId(2L);
            return language;
        });

        LanguageResponse response = languageService.createLanguage(request);

        assertThat(response.getCode()).isEqualTo("ja");
        assertThat(response.getName()).isEqualTo("Japanese");
    }

    @Test
    void createLanguage_whenCodeAlreadyExists_throwsDuplicateResourceException() {
        LanguageCreateRequest request = createRequest();
        when(languageRepository.existsByCode("ja")).thenReturn(true);

        assertThatThrownBy(() -> languageService.createLanguage(request)).isInstanceOf(DuplicateResourceException.class);
        verify(languageRepository, never()).save(any());
    }

    @Test
    void updateLanguage_whenFound_updatesFields() {
        Language language = activeLanguage();
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LanguageUpdateRequest request = new LanguageUpdateRequest();
        request.setName("English (US)");
        request.setStatus(LanguageStatus.INACTIVE);

        LanguageResponse response = languageService.updateLanguage(1L, request);

        assertThat(response.getName()).isEqualTo("English (US)");
        assertThat(response.getStatus()).isEqualTo(LanguageStatus.INACTIVE);
    }

    @Test
    void updateLanguage_whenNotFound_throwsResourceNotFoundException() {
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.updateLanguage(1L, new LanguageUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteLanguage_whenFound_softDeletes() {
        Language language = activeLanguage();
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> invocation.getArgument(0));

        languageService.deleteLanguage(1L);

        assertThat(language.isDeleted()).isTrue();
        assertThat(language.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteLanguage_whenNotFound_throwsResourceNotFoundException() {
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.deleteLanguage(1L)).isInstanceOf(ResourceNotFoundException.class);
        verify(languageRepository, never()).save(any());
    }
}
