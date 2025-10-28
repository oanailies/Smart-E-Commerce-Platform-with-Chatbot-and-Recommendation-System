package review_service.domain.model;

import jakarta.persistence.*;
@Entity
@Table(name = "review_reactions")
public class ReviewReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reviewId;
    private Long clientId;
    private Boolean liked;

    public ReviewReaction() {}

    public ReviewReaction(Long reviewId, Long clientId, Boolean liked) {
        this.reviewId = reviewId;
        this.clientId = clientId;
        this.liked = liked;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}
