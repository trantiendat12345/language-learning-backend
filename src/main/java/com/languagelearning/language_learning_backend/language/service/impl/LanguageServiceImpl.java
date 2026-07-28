package com.languagelearning.language_learning_backend.language.service.impl;

import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageCreateRequest;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageUpdateRequest;
import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import com.languagelearning.language_learning_backend.language.mapper.LanguageMapper;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.language.service.LanguageService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private static final String LANGUAGE_CODE_TAKEN_MESSAGE = "Language code đã tồn tại";

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getActiveLanguages() {
        return languageRepository.findAllByStatus(LanguageStatus.ACTIVE).stream()
                .map(languageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getAllLanguagesForAdmin() {
        return languageRepository.findAll().stream().map(languageMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageResponse getLanguageByIdForAdmin(Long id) {
        return languageMapper.toResponse(findLanguageOrThrow(id));
    }

    @Override
    @Transactional
    public LanguageResponse createLanguage(LanguageCreateRequest request) {
        if (languageRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(LANGUAGE_CODE_TAKEN_MESSAGE);
        }

        Language language = new Language();
        language.setCode(request.getCode());
        language.setName(request.getName());
        language.setFlagIconUrl(request.getFlagIconUrl());
        return languageMapper.toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponse updateLanguage(Long id, LanguageUpdateRequest request) {
        Language language = findLanguageOrThrow(id);
        language.setName(request.getName());
        language.setFlagIconUrl(request.getFlagIconUrl());
        language.setStatus(request.getStatus());
        return languageMapper.toResponse(languageRepository.save(language));
    }

    /**
     * Soft-delete (D9) - `deletedAt` không được Spring Data JPA Auditing tự set (chỉ
     * `createdAt`/`updatedAt` có `@CreatedDate`/`@LastModifiedDate`), nên phải set thủ công.
     * `deletedBy` cố tình để trống - `updatedBy` đã tự động ghi nhận admin thực hiện thao
     * tác này qua `@LastModifiedBy` trong cùng lần `save()`, không cần trace 2 lần.
     */
    @Override
    @Transactional
    public void deleteLanguage(Long id) {
        Language language = findLanguageOrThrow(id);
        language.setDeleted(true);
        language.setDeletedAt(LocalDateTime.now());
        languageRepository.save(language);
    }

    private Language findLanguageOrThrow(Long id) {
        return languageRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
