package review_service.application.controller;

        import review_service.application.dto.ReviewWithClientNameDTO;
        import review_service.application.service.ReviewService;
        import review_service.domain.model.Review;
        import review_service.domain.model.ReviewReaction;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import review_service.domain.repository.ReviewRepository;

        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping
    public ResponseEntity<Review> createReview(
            @RequestHeader("Authorization") String token,
            @RequestBody Review reviewRequest) {
        Review review = reviewService.createReview(
                reviewRequest.getClientId(),
                reviewRequest.getOrderId(),
                reviewRequest.getProductId(),
                reviewRequest.getRating(),
                reviewRequest.getComment(),
                token
        );
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Review>> getReviewsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(reviewService.getReviewsByClient(clientId));
    }

    @GetMapping("/product/{productId}/with-client-name")
    public ResponseEntity<List<ReviewWithClientNameDTO>> getReviewsWithClientName(
            @PathVariable Long productId,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(reviewService.getReviewsWithClientName(productId, clientId));
    }

    @GetMapping("/product/{productId}/with-client-name/sorted-desc")
    public ResponseEntity<List<ReviewWithClientNameDTO>> getReviewsSortedByRatingDesc(
            @PathVariable Long productId,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(reviewService.getReviewsSortedByRatingDesc(productId, clientId));
    }

    @GetMapping("/product/{productId}/with-client-name/sorted-asc")
    public ResponseEntity<List<ReviewWithClientNameDTO>> getReviewsSortedByRatingAsc(
            @PathVariable Long productId,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(reviewService.getReviewsSortedByRatingAsc(productId, clientId));
    }

    @PostMapping("/products/average-ratings")
    public ResponseEntity<Map<Long, Double>> getAverageRatings(@RequestBody List<Long> productIds) {
        Map<Long, Double> result = new HashMap<>();
        for (Long productId : productIds) {
            Double avg = reviewRepository.findAverageRatingByProductId(productId);
            result.put(productId, avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<?> likeReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String token,
            @RequestParam Long clientId) {
        Review review = reviewService.likeReview(reviewId, clientId, token);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{reviewId}/dislike")
    public ResponseEntity<?> dislikeReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String token,
            @RequestParam Long clientId) {
        Review review = reviewService.dislikeReview(reviewId, clientId, token);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{reviewId}/reaction")
    public ResponseEntity<ReviewReaction> getClientReaction(
            @PathVariable Long reviewId,
            @RequestParam Long clientId) {
        Optional<ReviewReaction> reaction = reviewService.getClientReaction(reviewId, clientId);
        return reaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        double rounded = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        return ResponseEntity.ok(rounded);
    }

}
