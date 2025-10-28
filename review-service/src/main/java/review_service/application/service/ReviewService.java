package review_service.application.service;

import review_service.application.dto.ReviewWithClientNameDTO;
import review_service.application.dto.OrderDTO;
import review_service.domain.model.Review;
import review_service.domain.model.ReviewReaction;
import review_service.domain.repository.ReviewReactionRepository;
import review_service.domain.repository.ReviewRepository;
import review_service.infrastructure.client.UserClient;
import review_service.infrastructure.client.OrderClient;
import review_service.infrastructure.client.EmailClient;
import review_service.infrastructure.client.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReactionRepository reactionRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private ProductClient productClient;

    public Review createReview(Long clientId, Long orderId, Long productId,
                               int rating, String comment, String jwtToken) {
        boolean clientExists = userClient.checkClientExists(clientId, jwtToken);
        if (!clientExists) {
            throw new RuntimeException("Client not found: " + clientId);
        }
        OrderDTO order = orderClient.getOrderById(orderId, jwtToken);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (!order.getClientId().equals(clientId)) {
            throw new RuntimeException("Client " + clientId + " has not placed this order: " + orderId);
        }

        boolean productExistsInOrder = order.getOrderProducts().stream()
                .anyMatch(op -> op.getProductId().equals(productId));

        if (!productExistsInOrder) {
            throw new RuntimeException("Product " + productId + " not found in order " + orderId);
        }

        Review review = new Review();
        review.setClientId(clientId);
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(new Date());

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getReviewsByClient(Long clientId) {
        return reviewRepository.findByClientId(clientId);
    }

    public List<ReviewWithClientNameDTO> getReviewsWithClientName(Long productId, Long currentClientId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        return reviews.stream().map(review -> {
            String clientName = userClient.getClientNameById(review.getClientId());
            Optional<ReviewReaction> reaction =
                    currentClientId != null
                            ? reactionRepository.findByReviewIdAndClientId(review.getId(), currentClientId)
                            : Optional.empty();

            Boolean userReaction = reaction.map(ReviewReaction::getLiked).orElse(null);

            return new ReviewWithClientNameDTO(
                    review.getId(),
                    review.getClientId(),
                    clientName,
                    review.getRating(),
                    review.getComment(),
                    review.getReviewDate(),
                    review.getLikeCount(),
                    review.getDislikeCount(),
                    userReaction
            );
        }).toList();
    }

    public List<ReviewWithClientNameDTO> getReviewsSortedByRatingDesc(Long productId, Long currentClientId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByRatingDesc(productId);
        return mapReviewsToDTOs(reviews, currentClientId);
    }

    public List<ReviewWithClientNameDTO> getReviewsSortedByRatingAsc(Long productId, Long currentClientId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByRatingAsc(productId);
        return mapReviewsToDTOs(reviews, currentClientId);
    }

    private List<ReviewWithClientNameDTO> mapReviewsToDTOs(List<Review> reviews, Long currentClientId) {
        return reviews.stream().map(review -> {
            String clientName = userClient.getClientNameById(review.getClientId());
            Optional<ReviewReaction> reaction =
                    currentClientId != null
                            ? reactionRepository.findByReviewIdAndClientId(review.getId(), currentClientId)
                            : Optional.empty();

            Boolean userReaction = reaction.map(ReviewReaction::getLiked).orElse(null);

            return new ReviewWithClientNameDTO(
                    review.getId(),
                    review.getClientId(),
                    clientName,
                    review.getRating(),
                    review.getComment(),
                    review.getReviewDate(),
                    review.getLikeCount(),
                    review.getDislikeCount(),
                    userReaction
            );
        }).toList();
    }

    public Double getAverageRating(Long productId) {
        Double average = reviewRepository.findAverageRatingByProductId(productId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    public Review likeReview(Long reviewId, Long clientId, String jwtToken) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Optional<ReviewReaction> existing = reactionRepository.findByReviewIdAndClientId(reviewId, clientId);

        if (existing.isPresent()) {
            ReviewReaction reaction = existing.get();
            if (Boolean.TRUE.equals(reaction.getLiked())) {
                reactionRepository.delete(reaction);
                review.setLikeCount(Math.max(0, Objects.requireNonNullElse(review.getLikeCount(), 0) - 1));
            } else {
                reaction.setLiked(true);
                reactionRepository.save(reaction);
                review.setLikeCount(Objects.requireNonNullElse(review.getLikeCount(), 0) + 1);
                review.setDislikeCount(Math.max(0, Objects.requireNonNullElse(review.getDislikeCount(), 0) - 1));
                sendReactionNotification(review, clientId, true, jwtToken);
            }
        } else {
            reactionRepository.save(new ReviewReaction(reviewId, clientId, true));
            review.setLikeCount(Objects.requireNonNullElse(review.getLikeCount(), 0) + 1);
            sendReactionNotification(review, clientId, true, jwtToken);
        }

        return reviewRepository.save(review);
    }

    public Review dislikeReview(Long reviewId, Long clientId, String jwtToken) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Optional<ReviewReaction> existing = reactionRepository.findByReviewIdAndClientId(reviewId, clientId);

        if (existing.isPresent()) {
            ReviewReaction reaction = existing.get();
            if (Boolean.FALSE.equals(reaction.getLiked())) {
                reactionRepository.delete(reaction);
                review.setDislikeCount(Math.max(0, Objects.requireNonNullElse(review.getDislikeCount(), 0) - 1));
            } else {
                reaction.setLiked(false);
                reactionRepository.save(reaction);
                review.setDislikeCount(Objects.requireNonNullElse(review.getDislikeCount(), 0) + 1);
                review.setLikeCount(Math.max(0, Objects.requireNonNullElse(review.getLikeCount(), 0) - 1));
                sendReactionNotification(review, clientId, false, jwtToken);
            }
        } else {
            reactionRepository.save(new ReviewReaction(reviewId, clientId, false));
            review.setDislikeCount(Objects.requireNonNullElse(review.getDislikeCount(), 0) + 1);
            sendReactionNotification(review, clientId, false, jwtToken);
        }

        return reviewRepository.save(review);
    }

    private void sendReactionNotification(Review review, Long reactorClientId, boolean liked, String jwtToken) {
        try {
            Long authorId = review.getClientId();
            String authorEmail = userClient.getClientEmailById(authorId, jwtToken);
            String authorName = userClient.getClientNameById(authorId);
            String reactorName = userClient.getClientNameById(reactorClientId);
            String productName = productClient.getFullProductById(review.getProductId(), jwtToken).getName();

            String subject;
            String message;

            if (liked) {
                subject = "Your opinion matters!";
                message = String.format(
                        "Hello %s,\n\nUser %s has just LIKED your review on product: %s.\n\n" +
                                "Your opinion matters to our community, thank you for sharing it!\n\n" +
                                "Best regards,\nBeautyShop Team",
                        authorName, reactorName, productName
                );
            } else {
                subject = "Keep sharing your thoughts!";
                message = String.format(
                        "Hello %s,\n\nUser %s has just DISLIKED your review on product: %s.\n\n" +
                                "Don’t worry, not everyone has to agree with you. Your opinion is still valuable and helps our community see different perspectives.\n\n" +
                                "Best regards,\nBeautyShop Team",
                        authorName, reactorName, productName
                );
            }

            emailClient.sendEmail(authorEmail, subject, message);

        } catch (Exception e) {
            System.err.println("Failed to send reaction notification: " + e.getMessage());
        }
    }

    public Optional<ReviewReaction> getClientReaction(Long reviewId, Long clientId) {
        return reactionRepository.findByReviewIdAndClientId(reviewId, clientId);
    }
}
