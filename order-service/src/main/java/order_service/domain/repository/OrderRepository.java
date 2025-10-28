package order_service.domain.repository;

import order_service.domain.model.Order;
import order_service.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientId(Long clientId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderProducts WHERE o.id = :id")
    Optional<Order> findOrderWithProductsById(@Param("id") Long id);

    List<Order> findByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses);

    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);

}
