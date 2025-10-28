package order_service.application.controller;

import order_service.application.service.OrderService;
import order_service.domain.model.Order;
import order_service.domain.model.OrderProduct;
import order_service.domain.model.OrderStatus;
import order_service.infrastructure.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long clientId = ((Number) body.get("clientId")).longValue();
        double finalAmount = body.containsKey("finalAmount") ? ((Number) body.get("finalAmount")).doubleValue() : 0.0;

        List<OrderProduct> orderProducts = ((List<Map<String, Object>>) body.get("orderProducts"))
                .stream()
                .map(map -> {
                    OrderProduct op = new OrderProduct();
                    op.setProductId(((Number) map.get("productId")).longValue());
                    op.setQuantity(((Number) map.get("quantity")).intValue());
                    op.setSalePrice(((Number) map.get("salePrice")).doubleValue());
                    return op;
                })
                .toList();

        return ResponseEntity.ok(orderService.createOrder(clientId, orderProducts, finalAmount, token));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Order>> getOrdersByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getOrdersByClient(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<Order> completeOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        try {
            Order updated = orderService.markOrderAsCompleted(orderId, token);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        boolean removed = orderService.deleteOrder(orderId);
        if (removed) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{orderId}/toggle")
    public ResponseEntity<Order> toggleStatus(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            Order updated;
            if (order.getStatus() == OrderStatus.COMPLETED) {
                updated = orderService.cancelOrder(orderId);
            } else {
                updated = orderService.markOrderAsCompleted(orderId, token);
            }
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestBody String newStatus) {
        try {
            newStatus = newStatus.replace("\"", "");
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
            Order updated = orderService.updateStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Order> searchOrderById(@RequestParam("id") Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/cancel-with-email")
    public ResponseEntity<Order> cancelWithEmail(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        try {
            Order cancelled = orderService.cancelOrderWithEmail(orderId, token);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/return-request")
    public ResponseEntity<Order> requestReturn(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        try {
            Order returned = orderService.requestReturn(orderId, token);
            return ResponseEntity.ok(returned);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
