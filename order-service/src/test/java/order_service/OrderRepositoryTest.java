package order_service;

import order_service.domain.model.Order;
import order_service.domain.model.OrderProduct;
import order_service.domain.model.OrderStatus;
import order_service.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFindById_Success() {
        Order order = new Order();
        order.setClientId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(99.99);
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.CREATED, found.get().getStatus());
        assertEquals(99.99, found.get().getTotalPrice());
    }

    @Test
    void findByClientId_ReturnsOrders() {
        Order order1 = new Order();
        order1.setClientId(2L);
        order1.setStatus(OrderStatus.CREATED);
        Order order2 = new Order();
        order2.setClientId(2L);
        order2.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order1);
        orderRepository.save(order2);
        List<Order> results = orderRepository.findByClientId(2L);
        assertEquals(2, results.size());
    }

    @Test
    void findByClientIdAndStatusIn_FiltersCorrectly() {
        Order order1 = new Order();
        order1.setClientId(3L);
        order1.setStatus(OrderStatus.CREATED);

        Order order2 = new Order();
        order2.setClientId(3L);
        order2.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> results = orderRepository.findByClientIdAndStatusIn(3L,
                List.of(OrderStatus.CANCELLED));

        assertEquals(1, results.size());
        assertEquals(OrderStatus.CANCELLED, results.get(0).getStatus());
    }

    @Test
    void findOrderWithProductsById_FetchesProducts() {
        Order order = new Order();
        order.setClientId(4L);
        order.setStatus(OrderStatus.CREATED);

        OrderProduct op = new OrderProduct();
        op.setProductName("Perfume");
        op.setQuantity(1);
        op.setSalePrice(150.0);
        op.setOrder(order);

        order.setOrderProducts(List.of(op));

        Order saved = orderRepository.save(order);

        Optional<Order> found = orderRepository.findOrderWithProductsById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getOrderProducts().size());
    }
}
