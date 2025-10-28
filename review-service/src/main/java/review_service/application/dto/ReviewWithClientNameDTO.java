package review_service.application.dto;

import java.util.Date;

public class ReviewWithClientNameDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private int rating;
    private String comment;
    private Date reviewDate;
    private Boolean userReaction;
    private int likeCount;
    private int dislikeCount;

    public ReviewWithClientNameDTO(Long id, Long clientId, String clientName, int rating,
                                   String comment, Date reviewDate,
                                   int likeCount, int dislikeCount,
                                   Boolean userReaction) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.userReaction = userReaction;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getReviewDate() { return reviewDate; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }

    public Boolean getUserReaction() { return userReaction; }
    public void setUserReaction(Boolean userReaction) { this.userReaction = userReaction; }

    @Override
    public String toString() {
        return "ReviewWithClientNameDTO{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", clientName='" + clientName + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate=" + reviewDate +
                ", likeCount=" + likeCount +
                ", dislikeCount=" + dislikeCount +
                ", userReaction=" + userReaction +
                '}';
    }
}
