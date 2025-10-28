package delivery_payment_service.application.controller;

import delivery_payment_service.application.service.DeliveryService;
import delivery_payment_service.domain.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long id) {
        Delivery delivery = deliveryService.getDeliveryById(id);
        return delivery != null ? ResponseEntity.ok(delivery) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createDelivery(
            @RequestBody Delivery delivery,
            @RequestHeader("Authorization") String token) {
        try {
            Delivery saved = deliveryService.createDelivery(delivery, token);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDelivery(
            @PathVariable Long id,
            @RequestBody Delivery updatedDelivery) {
        try {
            Delivery result = deliveryService.updateDelivery(id, updatedDelivery);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Delivery> getDeliveryByOrderId(@PathVariable Long orderId) {
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        return delivery != null ? ResponseEntity.ok(delivery) : ResponseEntity.notFound().build();
    }


}
