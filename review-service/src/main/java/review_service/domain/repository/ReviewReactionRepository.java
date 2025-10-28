package review_service.domain.repository;

import review_service.domain.model.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {
    boolean existsByReviewIdAndClientId(Long reviewId, Long clientId);
    Optional<ReviewReaction> findByReviewIdAndClientId(Long reviewId, Long clientId);
    void deleteByReviewIdAndClientId(Long reviewId, Long clientId);
}
