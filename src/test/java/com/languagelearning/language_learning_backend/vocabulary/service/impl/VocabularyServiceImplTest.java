package com.languagelearning.language_learning_backend.vocabulary.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyCreateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyUpdateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import com.languagelearning.language_learning_backend.vocabulary.mapper.VocabularyMapper;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceImplTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private LanguageRepository languageRepository;

    private VocabularyServiceImpl vocabularyService;

    @BeforeEach
    void setUp() {
        VocabularyMapper vocabularyMapper = Mappers.getMapper(VocabularyMapper.class);
        vocabularyService = new VocabularyServiceImpl(vocabularyRepository, languageRepository, vocabularyMapper);
    }

    private Language language() {
        Language language = new Language();
        language.setId(1L);
        language.setCode("en");
        language.setName("English");
        return language;
    }

    private Vocabulary systemWord() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(1L);
        vocabulary.setLanguage(language());
        vocabulary.setWord("hello");
        vocabulary.setMeaning("xin chào");
        vocabulary.setStatus(VocabularyStatus.ACTIVE);
        return vocabulary;
    }

    private Vocabulary customWord() {
        Vocabulary vocabulary = systemWord();
        User owner = new User();
        owner.setId(2L);
        vocabulary.setOwner(owner);
        return vocabulary;
    }

    @Test
    void getSystemVocabularies_returnsMappedPage() {
        Page<Vocabulary> page = new PageImpl<>(List.of(systemWord()));
        when(vocabularyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = vocabularyService.getSystemVocabularies(null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getWord()).isEqualTo("hello");
    }

    @Test
    void getSystemVocabularyById_whenActiveSystemWord_returnsResponse() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(systemWord()));

        VocabularyResponse response = vocabularyService.getSystemVocabularyById(1L);

        assertThat(response.getWord()).isEqualTo("hello");
        assertThat(response.getLanguageCode()).isEqualTo("en");
    }

    @Test
    void getSystemVocabularyById_whenArchived_throwsResourceNotFoundException() {
        Vocabulary vocabulary = systemWord();
        vocabulary.setStatus(VocabularyStatus.ARCHIVED);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));

        assertThatThrownBy(() -> vocabularyService.getSystemVocabularyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSystemVocabularyById_whenCustomWord_throwsResourceNotFoundException() {
        Vocabulary vocabulary = systemWord();
        User owner = new User();
        owner.setId(2L);
        vocabulary.setOwner(owner);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));

        assertThatThrownBy(() -> vocabularyService.getSystemVocabularyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSystemVocabularyById_whenNotFound_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.getSystemVocabularyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getVocabularyByIdForAdmin_returnsRegardlessOfStatus() {
        Vocabulary vocabulary = systemWord();
        vocabulary.setStatus(VocabularyStatus.ARCHIVED);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));

        VocabularyResponse response = vocabularyService.getVocabularyByIdForAdmin(1L);

        assertThat(response.getStatus()).isEqualTo(VocabularyStatus.ARCHIVED);
    }

    @Test
    void getVocabularyByIdForAdmin_whenCustomWord_throwsResourceNotFoundException() {
        Vocabulary vocabulary = customWord();
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));

        assertThatThrownBy(() -> vocabularyService.getVocabularyByIdForAdmin(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllVocabulariesForAdmin_filtersOutCustomWords() {
        when(vocabularyRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(systemWord())));

        var result = vocabularyService.getAllVocabulariesForAdmin(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    private VocabularyCreateRequest createRequest() {
        VocabularyCreateRequest request = new VocabularyCreateRequest();
        request.setLanguageId(1L);
        request.setWord("hello");
        request.setMeaning("xin chào");
        return request;
    }

    @Test
    void createVocabulary_savesAndReturnsMapped() {
        VocabularyCreateRequest request = createRequest();
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> {
            Vocabulary vocabulary = invocation.getArgument(0);
            vocabulary.setId(1L);
            return vocabulary;
        });

        VocabularyResponse response = vocabularyService.createVocabulary(request);

        assertThat(response.getWord()).isEqualTo("hello");
        assertThat(response.getLanguageCode()).isEqualTo("en");
    }

    @Test
    void createVocabulary_whenLanguageNotFound_throwsResourceNotFoundException() {
        VocabularyCreateRequest request = createRequest();
        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.createVocabulary(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateVocabulary_whenFound_updatesFields() {
        Vocabulary vocabulary = systemWord();
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VocabularyUpdateRequest request = new VocabularyUpdateRequest();
        request.setLanguageId(1L);
        request.setWord("world");
        request.setMeaning("thế giới");
        request.setStatus(VocabularyStatus.ARCHIVED);

        VocabularyResponse response = vocabularyService.updateVocabulary(1L, request);

        assertThat(response.getWord()).isEqualTo("world");
        assertThat(response.getStatus()).isEqualTo(VocabularyStatus.ARCHIVED);
    }

    @Test
    void updateVocabulary_whenNotFound_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.updateVocabulary(1L, new VocabularyUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateVocabulary_whenCustomWord_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(customWord()));

        assertThatThrownBy(() -> vocabularyService.updateVocabulary(1L, new VocabularyUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteVocabulary_whenFound_softDeletes() {
        Vocabulary vocabulary = systemWord();
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabulary));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vocabularyService.deleteVocabulary(1L);

        assertThat(vocabulary.isDeleted()).isTrue();
        assertThat(vocabulary.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteVocabulary_whenNotFound_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteVocabulary_whenCustomWord_throwsResourceNotFoundException() {
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(customWord()));

        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
