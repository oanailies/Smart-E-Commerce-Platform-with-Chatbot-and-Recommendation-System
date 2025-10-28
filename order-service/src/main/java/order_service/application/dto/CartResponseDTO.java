package order_service.application.dto;

import order_service.domain.model.Cart;
import order_service.domain.model.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartResponseDTO {
    private Long id;
    private Long clientId;
    private String status;
    private List<CartItemDTO> cartItems;

    public CartResponseDTO(Cart cart) {
        this.id = cart.getId();
        this.clientId = cart.getClientId();
        this.status = cart.getStatus();
        this.cartItems = cart.getCartItems().stream()
                .map(CartItemDTO::new)
                .collect(Collectors.toList());
    }

    public static CartResponseDTO fromCart(Cart cart) {
        return new CartResponseDTO(cart);
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getStatus() { return status; }
    public List<CartItemDTO> getCartItems() { return cartItems; }
}
