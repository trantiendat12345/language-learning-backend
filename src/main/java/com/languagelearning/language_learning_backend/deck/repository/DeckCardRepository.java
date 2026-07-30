package com.languagelearning.language_learning_backend.deck.repository;

import com.languagelearning.language_learning_backend.deck.entity.DeckCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckCardRepository extends JpaRepository<DeckCard, Long> {

    List<DeckCard> findAllByDeckIdOrderByDisplayOrderAsc(Long deckId);

    boolean existsByDeckIdAndVocabularyId(Long deckId, Long vocabularyId);

    Optional<DeckCard> findByIdAndDeckId(Long id, Long deckId);

    long countByDeckId(Long deckId);
}
