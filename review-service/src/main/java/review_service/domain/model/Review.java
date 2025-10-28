package review_service.domain.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long productId;
    private Long clientId;
    private int rating;
    private String comment;
    private Date reviewDate;

    @Column(nullable = true)
    private Integer likeCount;

    @Column(nullable = true)
    private Integer dislikeCount;

    public Review() {}

    public Review(Long orderId, Long productId, Long clientId, int rating, String comment, Date reviewDate) {
        this.orderId = orderId;
        this.productId = productId;
        this.clientId = clientId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.likeCount = 0;
        this.dislikeCount = 0;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getProductId() { return productId; }
    public Long getClientId() { return clientId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Date getReviewDate() { return reviewDate; }


    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getLikeCount() {
        return likeCount != null ? likeCount : 0;
    }

    public int getDislikeCount() {
        return dislikeCount != null ? dislikeCount : 0;
    }

    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }

    public void setId(Long id) { this.id = id; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }
}
