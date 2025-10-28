package product_service.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import product_service.domain.model.FavoriteProduct;

import java.util.List;
import java.util.Optional;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {
    List<FavoriteProduct> findByClientId(Long clientId);
    Optional<FavoriteProduct> findByClientIdAndProductId(Long clientId, Long productId);
    void deleteByClientIdAndProductId(Long clientId, Long productId);
    List<FavoriteProduct> findByProductId(Long productId);

}
