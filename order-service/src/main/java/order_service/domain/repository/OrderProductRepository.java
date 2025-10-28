package order_service.domain.repository;

import order_service.domain.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("SELECT COUNT(op) > 0 FROM OrderProduct op WHERE op.order.id = :orderId AND op.order.clientId = :clientId AND op.productId = :productId")
    boolean existsByOrderIdAndClientIdAndProductId(@Param("orderId") Long orderId, @Param("clientId") Long clientId, @Param("productId") Long productId);
}
