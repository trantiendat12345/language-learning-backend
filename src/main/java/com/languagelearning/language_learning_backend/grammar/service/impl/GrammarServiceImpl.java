package com.languagelearning.language_learning_backend.grammar.service.impl;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarCreateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarExampleRequest;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarUpdateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarSummaryResponse;
import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import com.languagelearning.language_learning_backend.grammar.entity.GrammarExample;
import com.languagelearning.language_learning_backend.grammar.mapper.GrammarMapper;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarExampleRepository;
import com.languagelearning.language_learning_backend.grammar.repository.GrammarRepository;
import com.languagelearning.language_learning_backend.grammar.service.GrammarService;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GrammarServiceImpl implements GrammarService {

    private final GrammarRepository grammarRepository;
    private final GrammarExampleRepository grammarExampleRepository;
    private final LessonRepository lessonRepository;
    private final GrammarMapper grammarMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GrammarSummaryResponse> getGrammarsByLessonForAdmin(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException();
        }
        return grammarRepository.findAllByLessonIdOrderByDisplayOrderAsc(lessonId).stream()
                .map(grammarMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GrammarResponse getGrammarByIdForAdmin(Long id) {
        return toGrammarResponse(findGrammarOrThrow(id));
    }

    @Override
    @Transactional
    public GrammarResponse createGrammar(Long lessonId, GrammarCreateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(ResourceNotFoundException::new);

        Grammar grammar = new Grammar();
        grammar.setLesson(lesson);
        grammar.setTitle(request.getTitle());
        grammar.setPattern(request.getPattern());
        grammar.setExplanation(request.getExplanation());
        grammar.setDifficulty(request.getDifficulty());
        grammar.setDisplayOrder(request.getDisplayOrder());
        grammar = grammarRepository.save(grammar);

        List<GrammarExample> examples = saveExamples(grammar, request.getExamples());
        return grammarMapper.toResponse(grammar, grammarMapper.toExampleResponseList(examples));
    }

    @Override
    @Transactional
    public GrammarResponse updateGrammar(Long id, GrammarUpdateRequest request) {
        Grammar grammar = findGrammarOrThrow(id);
        grammar.setTitle(request.getTitle());
        grammar.setPattern(request.getPattern());
        grammar.setExplanation(request.getExplanation());
        grammar.setDifficulty(request.getDifficulty());
        grammar.setDisplayOrder(request.getDisplayOrder());
        grammar = grammarRepository.save(grammar);

        // Update = thay toàn bộ danh sách example (không sửa/xoá riêng lẻ từng example).
        softDeleteExamples(grammarExampleRepository.findAllByGrammarId(id));
        List<GrammarExample> examples = saveExamples(grammar, request.getExamples());
        return grammarMapper.toResponse(grammar, grammarMapper.toExampleResponseList(examples));
    }

    @Override
    @Transactional
    public void deleteGrammar(Long id) {
        Grammar grammar = findGrammarOrThrow(id);
        grammar.setDeleted(true);
        grammar.setDeletedAt(LocalDateTime.now());
        grammarRepository.save(grammar);
        softDeleteExamples(grammarExampleRepository.findAllByGrammarId(id));
    }

    private GrammarResponse toGrammarResponse(Grammar grammar) {
        List<GrammarExample> examples = grammarExampleRepository.findAllByGrammarId(grammar.getId());
        return grammarMapper.toResponse(grammar, grammarMapper.toExampleResponseList(examples));
    }

    private List<GrammarExample> saveExamples(Grammar grammar, List<GrammarExampleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<GrammarExample> examples = requests.stream()
                .map(request -> {
                    GrammarExample example = new GrammarExample();
                    example.setGrammar(grammar);
                    example.setExampleText(request.getExampleText());
                    example.setTranslation(request.getTranslation());
                    example.setNote(request.getNote());
                    return example;
                })
                .toList();
        return grammarExampleRepository.saveAll(examples);
    }

    private void softDeleteExamples(List<GrammarExample> examples) {
        LocalDateTime now = LocalDateTime.now();
        examples.forEach(example -> {
            example.setDeleted(true);
            example.setDeletedAt(now);
        });
        grammarExampleRepository.saveAll(examples);
    }

    private Grammar findGrammarOrThrow(Long id) {
        return grammarRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
