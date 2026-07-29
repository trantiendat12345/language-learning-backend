package com.languagelearning.language_learning_backend.lesson.mapper;

import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonVocabularyResponse;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "courseId", source = "lesson.course.id")
    @Mapping(target = "enrolled", source = "enrolled")
    @Mapping(target = "vocabularies", source = "vocabularies")
    @Mapping(target = "grammars", source = "grammars")
    LessonResponse toResponse(
            Lesson lesson, boolean enrolled, List<LessonVocabularyResponse> vocabularies, List<GrammarResponse> grammars);

    LessonSummaryResponse toSummaryResponse(Lesson lesson);
}
