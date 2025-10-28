package order_service;

import order_service.application.service.OrderService;
import order_service.domain.model.Order;
import order_service.domain.model.OrderProduct;
import order_service.domain.model.OrderStatus;
import order_service.domain.repository.OrderRepository;
import order_service.domain.repository.OrderProductRepository;
import order_service.infrastructure.client.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderProductRepository orderProductRepository;
    @Mock private ProductClient productClient;
    @Mock private UserClient userClient;
    @Mock private EmailClient emailClient;
    @Mock private DeliveryPaymentClient deliveryPaymentClient;

    @InjectMocks
    private OrderService orderService;

    // === createOrder ===
    @Test
    void createOrder_Success() {
        OrderProduct op = new OrderProduct();
        op.setProductName("Lipstick");
        op.setBrandName("L'Oreal");
        op.setQuantity(1);
        op.setSalePrice(25.0);

        when(userClient.checkClientExists(eq(1L), anyString())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(1L, List.of(op), 25.0, "jwt");

        assertNotNull(order);
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(25.0, order.getTotalPrice());
        assertEquals(1, order.getOrderProducts().size());
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_ClientNotFound() {
        when(userClient.checkClientExists(eq(99L), anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(99L, List.of(), 0, "jwt"));

        assertTrue(ex.getMessage().contains("Client not found"));
    }

    @Test
    void markOrderAsCompleted_Success() {
        Order order = new Order();
        order.setId(2L);
        order.setClientId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderProducts(List.of());

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userClient.getClientEmailById(anyLong(), anyString())).thenReturn("test@mail.com");

        Order completed = orderService.markOrderAsCompleted(2L, "jwt");

        assertEquals(OrderStatus.COMPLETED, completed.getStatus());
        verify(orderRepository).save(order);
        verify(emailClient, atLeastOnce()).sendEmailToClient(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void markOrderAsCompleted_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.markOrderAsCompleted(99L, "jwt"));
        assertTrue(ex.getMessage().contains("Order not found"));}


    @Test
    void cancelOrder_Success() {
        Order order = new Order();
        order.setId(3L);
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        Order cancelled = orderService.cancelOrder(3L);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        verify(orderRepository).save(order);}

    @Test
    void cancelOrder_AlreadyCompleted() {
        Order order = new Order();
        order.setId(4L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(4L));
        assertTrue(ex.getMessage().contains("already completed"));}

    @Test
    void cancelOrderWithEmail_Success() {
        Order order = new Order();
        order.setId(5L);
        order.setClientId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderProducts(List.of());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userClient.getClientEmailById(anyLong(), anyString())).thenReturn("client@mail.com");

        Order cancelled = orderService.cancelOrderWithEmail(5L, "jwt");

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        verify(emailClient, atLeastOnce()).sendEmailToClient(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void cancelOrderWithEmail_NotFound() {
        when(orderRepository.findById(123L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrderWithEmail(123L, "jwt"));

        assertTrue(ex.getMessage().contains("Order not found"));
    }

    @Test
    void deleteOrder_Success() {
        when(orderRepository.existsById(10L)).thenReturn(true);

        boolean result = orderService.deleteOrder(10L);

        assertTrue(result);
        verify(orderRepository).deleteById(10L);
    }

    @Test
    void deleteOrder_NotFound() {
        when(orderRepository.existsById(11L)).thenReturn(false);

        boolean result = orderService.deleteOrder(11L);

        assertFalse(result);
        verify(orderRepository, never()).deleteById(11L);
    }

    @Test
    void updateStatus_Success() {
        Order order = new Order();
        order.setId(6L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = orderService.updateStatus(6L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void updateStatus_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.updateStatus(999L, OrderStatus.CANCELLED));

        assertTrue(ex.getMessage().contains("Order not found"));
    }
}
