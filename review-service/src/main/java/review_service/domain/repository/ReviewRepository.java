package review_service.domain.repository;


import review_service.domain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.productId = :productId")
    List<Review> findByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.clientId = :clientId")
    List<Review> findByClientId(@Param("clientId") Long clientId);

    List<Review> findByProductIdOrderByRatingDesc(Long productId);
    List<Review> findByProductIdOrderByRatingAsc(Long productId);



    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

}
