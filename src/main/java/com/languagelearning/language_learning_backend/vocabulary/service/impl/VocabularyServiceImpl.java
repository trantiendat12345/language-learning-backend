package com.languagelearning.language_learning_backend.vocabulary.service.impl;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyCreateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyUpdateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularySummaryResponse;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import com.languagelearning.language_learning_backend.vocabulary.mapper.VocabularyMapper;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularySpecification;
import com.languagelearning.language_learning_backend.vocabulary.service.VocabularyService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final LanguageRepository languageRepository;
    private final VocabularyMapper vocabularyMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularySummaryResponse> getSystemVocabularies(Long languageId, String keyword, Pageable pageable) {
        Specification<Vocabulary> spec = Specification.allOf(
                VocabularySpecification.hasStatus(VocabularyStatus.ACTIVE),
                VocabularySpecification.isSystemWord(),
                VocabularySpecification.hasLanguageId(languageId),
                VocabularySpecification.wordOrMeaningContains(keyword));
        Page<VocabularySummaryResponse> page =
                vocabularyRepository.findAll(spec, pageable).map(vocabularyMapper::toSummaryResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyResponse getSystemVocabularyById(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (vocabulary.getOwner() != null || vocabulary.getStatus() != VocabularyStatus.ACTIVE) {
            // Không tiết lộ từ ARCHIVED/custom của User khác tồn tại - trả cùng lỗi với id không tồn tại.
            throw new ResourceNotFoundException();
        }
        return vocabularyMapper.toResponse(vocabulary);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularySummaryResponse> getAllVocabulariesForAdmin(Pageable pageable) {
        return PageResponse.from(vocabularyRepository.findAll(pageable).map(vocabularyMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyByIdForAdmin(Long id) {
        return vocabularyMapper.toResponse(findVocabularyOrThrow(id));
    }

    @Override
    @Transactional
    public VocabularyResponse createVocabulary(VocabularyCreateRequest request) {
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setLanguage(language);
        vocabulary.setWord(request.getWord());
        vocabulary.setMeaning(request.getMeaning());
        vocabulary.setIpa(request.getIpa());
        vocabulary.setPronunciationAudioUrl(request.getPronunciationAudioUrl());
        vocabulary.setWordType(request.getWordType());
        vocabulary.setImageUrl(request.getImageUrl());
        vocabulary.setDifficulty(request.getDifficulty());
        vocabulary.setExampleSentence(request.getExampleSentence());
        vocabulary.setExampleTranslation(request.getExampleTranslation());
        vocabulary.setFrequencyRank(request.getFrequencyRank());
        vocabulary = vocabularyRepository.save(vocabulary);

        return vocabularyMapper.toResponse(vocabulary);
    }

    @Override
    @Transactional
    public VocabularyResponse updateVocabulary(Long id, VocabularyUpdateRequest request) {
        Vocabulary vocabulary = findVocabularyOrThrow(id);
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);

        vocabulary.setLanguage(language);
        vocabulary.setWord(request.getWord());
        vocabulary.setMeaning(request.getMeaning());
        vocabulary.setIpa(request.getIpa());
        vocabulary.setPronunciationAudioUrl(request.getPronunciationAudioUrl());
        vocabulary.setWordType(request.getWordType());
        vocabulary.setImageUrl(request.getImageUrl());
        vocabulary.setDifficulty(request.getDifficulty());
        vocabulary.setExampleSentence(request.getExampleSentence());
        vocabulary.setExampleTranslation(request.getExampleTranslation());
        vocabulary.setFrequencyRank(request.getFrequencyRank());
        vocabulary.setStatus(request.getStatus());
        vocabulary = vocabularyRepository.save(vocabulary);

        return vocabularyMapper.toResponse(vocabulary);
    }

    /** Soft-delete (D9) - xem comment tương tự ở LanguageServiceImpl về deletedBy. */
    @Override
    @Transactional
    public void deleteVocabulary(Long id) {
        Vocabulary vocabulary = findVocabularyOrThrow(id);
        vocabulary.setDeleted(true);
        vocabulary.setDeletedAt(LocalDateTime.now());
        vocabularyRepository.save(vocabulary);
    }

    private Vocabulary findVocabularyOrThrow(Long id) {
        return vocabularyRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
