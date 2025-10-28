package order_service.application.dto;

import order_service.domain.model.Cart;
import order_service.domain.model.CartItem;

public class CartItemDTO {
    private Long id;
    private Long productId;
    private int quantity;
    private double price;

    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProductId();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getPrice();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }


}
