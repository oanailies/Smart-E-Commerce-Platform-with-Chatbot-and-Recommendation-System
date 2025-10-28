package order_service.domain.repository;

import order_service.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    Optional<Cart> findTopByClientIdAndStatusOrderByCreatedDateDesc(Long clientId, String status);
    Optional<Cart> findTopBySessionIdAndStatusOrderByCreatedDateDesc(String sessionId, String status);
    
    List<Cart> findAllByClientIdAndStatusOrderByCreatedDateDesc(Long clientId, String status);
    List<Cart> findAllBySessionIdAndStatusOrderByCreatedDateDesc(String sessionId, String status);
}
