package com.languagelearning.language_learning_backend.deck.repository;

import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import org.springframework.data.jpa.domain.Specification;

/** Filter động cho GET /api/decks (public search) và GET /api/decks/mine — theo đúng mẫu đặt tên ở docs/PROJECT_OVERVIEW.md mục 7.1. */
public final class DeckSpecification {

    private DeckSpecification() {
    }

    public static Specification<Deck> hasVisibility(DeckVisibility visibility) {
        return (root, query, cb) -> cb.equal(root.get("visibility"), visibility);
    }

    public static Specification<Deck> hasStatus(DeckStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Deck> hasOwnerId(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    public static Specification<Deck> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }
}
