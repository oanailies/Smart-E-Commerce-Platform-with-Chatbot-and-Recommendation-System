package order_service.application.controller;

import order_service.application.dto.CartResponseDTO;
import order_service.application.service.CartService;
import order_service.domain.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addProductToCart(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {

        Long clientId  = body.containsKey("clientId")  ? ((Number) body.get("clientId")).longValue() : null;
        String sessionId = (String) body.getOrDefault("sessionId", null);
        Long productId = ((Number) body.get("productId")).longValue();
        int quantity   = ((Number) body.get("quantity")).intValue();
        double price   = body.containsKey("price") ? ((Number) body.get("price")).doubleValue() : 0.0;

        return ResponseEntity.ok(
                cartService.addProductToCart(clientId, sessionId, productId, quantity, price, token)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<CartResponseDTO> getActiveCart(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (clientId == null && (sessionId == null || sessionId.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        cartService.deduplicateActiveCarts(clientId, sessionId);

        CartResponseDTO dto = cartService.getActiveCart(clientId, sessionId);
        if (dto == null) {
            dto = CartResponseDTO.fromCart(cartService.getOrCreateActiveCart(clientId, sessionId, token));
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(cartItemId, clientId, sessionId));
    }

    @PostMapping("/increment/{cartItemId}")
    public ResponseEntity<CartResponseDTO> incrementCartItem(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(cartItemId, clientId, sessionId, 1));
    }

    @PostMapping("/decrement/{cartItemId}")
    public ResponseEntity<CartResponseDTO> decrementCartItem(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(cartItemId, clientId, sessionId, -1));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkoutCart(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long clientId = ((Number) body.get("clientId")).longValue();
        String couponCode = body.containsKey("couponCode") ? (String) body.get("couponCode") : null;

        Boolean giftWrap = body.containsKey("giftWrap") && (Boolean) body.get("giftWrap");
        String personalizedMessage = (String) body.getOrDefault("personalizedMessage", null);

        return ResponseEntity.ok(cartService.checkout(clientId, token, couponCode, giftWrap, personalizedMessage));
    }



}
