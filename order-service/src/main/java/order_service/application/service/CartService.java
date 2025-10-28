package order_service.application.service;

import order_service.application.dto.CartResponseDTO;
import order_service.application.dto.ProductDTO;
import order_service.domain.model.*;
import order_service.domain.repository.CartItemRepository;
import order_service.domain.repository.CartRepository;
import order_service.domain.repository.OrderRepository;
import order_service.infrastructure.client.EmailClient;
import order_service.infrastructure.client.ProductClient;
import order_service.infrastructure.client.UserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final EmailClient emailClient;
    private final CouponService couponService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       OrderRepository orderRepository,
                       ProductClient productClient,
                       UserClient userClient,
                       EmailClient emailClient,
                       CouponService couponService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.userClient = userClient;
        this.emailClient = emailClient;
        this.couponService = couponService;
    }

    private static final String ACTIVE = "ACTIVE";
    private static final String CHECKED_OUT = "CHECKED_OUT";

    @Transactional
    public Cart getOrCreateActiveCart(Long clientId, String sessionId, String jwtToken) {
        if (clientId == null && (sessionId == null || sessionId.isBlank())) {
            throw new IllegalArgumentException("Either clientId or sessionId must be provided");
        }

        if (clientId != null) {
            Optional<Cart> found = cartRepository.findTopByClientIdAndStatusOrderByCreatedDateDesc(clientId, ACTIVE);
            if (found.isPresent()) return found.get();

            if (!userClient.checkClientExists(clientId, jwtToken)) {
                throw new RuntimeException("Client not found: " + clientId);
            }
            Cart c = new Cart();
            c.setClientId(clientId);
            c.setStatus(ACTIVE);
            c.setCreatedDate(new Date());
            return cartRepository.save(c);
        } else {
            Optional<Cart> found = cartRepository.findTopBySessionIdAndStatusOrderByCreatedDateDesc(sessionId, ACTIVE);
            if (found.isPresent()) return found.get();

            Cart c = new Cart();
            c.setSessionId(sessionId);
            c.setStatus(ACTIVE);
            c.setCreatedDate(new Date());
            return cartRepository.save(c);
        }
    }

    @Transactional
    public CartResponseDTO addProductToCart(Long clientId, String sessionId, Long productId, int quantity, double productPrice, String jwtToken) {
        Cart cart = getOrCreateActiveCart(clientId, sessionId, jwtToken);

        if (!productClient.checkProductExists(productId, jwtToken)) {
            throw new RuntimeException("Product not found: " + productId);
        }

        CartItem item = cart.getCartItems().stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem ci = new CartItem(cart, productId, 0, productPrice);
                    cart.getCartItems().add(ci);
                    return ci;
                });

        item.setQuantity(item.getQuantity() + quantity);
        item.setPrice(productPrice);
        cartItemRepository.save(item);

        return new CartResponseDTO(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public CartResponseDTO getActiveCart(Long clientId, String sessionId) {
        Optional<Cart> found = (clientId != null)
                ? cartRepository.findTopByClientIdAndStatusOrderByCreatedDateDesc(clientId, ACTIVE)
                : cartRepository.findTopBySessionIdAndStatusOrderByCreatedDateDesc(sessionId, ACTIVE);

        return found.map(CartResponseDTO::new).orElse(null);
    }

    @Transactional
    public CartResponseDTO removeProductFromCart(Long cartItemId, Long clientId, String sessionId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found: " + cartItemId));

        Cart cart = cartItem.getCart();
        if (clientId != null && cart.getClientId() != null && !clientId.equals(cart.getClientId())) {
            throw new RuntimeException("CartItem does not belong to this client");
        }
        if (sessionId != null && cart.getSessionId() != null && !sessionId.equals(cart.getSessionId())) {
            throw new RuntimeException("CartItem does not belong to this session");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return new CartResponseDTO(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponseDTO updateCartItemQuantity(Long cartItemId, Long clientId, String sessionId, int quantityChange) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = cartItem.getCart();
        if (clientId != null && cart.getClientId() != null && !clientId.equals(cart.getClientId())) {
            throw new RuntimeException("CartItem does not belong to this client");
        }
        if (sessionId != null && cart.getSessionId() != null && !sessionId.equals(cart.getSessionId())) {
            throw new RuntimeException("CartItem does not belong to this session");
        }

        int newQty = cartItem.getQuantity() + quantityChange;
        if (newQty > 0) {
            cartItem.setQuantity(newQty);
            cartItemRepository.save(cartItem);
        } else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return CartResponseDTO.fromCart(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public Order checkout(Long clientId, String jwtToken, String couponCode, Boolean giftWrap, String personalizedMessage) {
        Cart cart = cartRepository.findTopByClientIdAndStatusOrderByCreatedDateDesc(clientId, ACTIVE)
                .orElseThrow(() -> new RuntimeException("Cart is empty or not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setClientId(clientId);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.CREATED);

        final double[] subtotal = {0.0};
        Order finalOrder = order;

        List<OrderProduct> orderProducts = cart.getCartItems().stream().map(ci -> {
            ProductDTO productDTO = productClient.getFullProductById(ci.getProductId(), jwtToken);
            boolean success = productClient.decreaseStock(ci.getProductId(), ci.getQuantity(), jwtToken);
            if (!success) {
                throw new RuntimeException("Insufficient stock for product ID " + ci.getProductId());
            }

            double price = productDTO.getPrice();
            if (productDTO.isOnSale() && productDTO.getDiscountedPrice() != null && productDTO.getDiscountedPrice() < price) {
                price = productDTO.getDiscountedPrice();
            }

            OrderProduct op = new OrderProduct();
            op.setOrder(finalOrder);
            op.setProductId(ci.getProductId());
            op.setQuantity(ci.getQuantity());
            op.setSalePrice(price);
            if (productDTO.getName() != null) op.setProductName(productDTO.getName());
            if (productDTO.getBrand() != null && productDTO.getBrand().getName() != null) op.setBrandName(productDTO.getBrand().getName());
            if (productDTO.getImages() != null && !productDTO.getImages().isEmpty() && productDTO.getImages().get(0).getImageUrl() != null) {
                op.setImageUrl(productDTO.getImages().get(0).getImageUrl());
            }
            if (productDTO.getShade() != null) {
                op.setShade(productDTO.getShade().getName());
            }
            if (productDTO.getSize() != null) {
                op.setSize(productDTO.getSize().getName());
            }

            subtotal[0] += price * ci.getQuantity();
            return op;
        }).collect(Collectors.toList());

        double deliveryCost = (subtotal[0] >= 100) ? 0.0 : 5.0;
        double discountedSubtotal = subtotal[0];
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponService.validateCoupon(couponCode);
            discountedSubtotal = couponService.applyDiscount(coupon, discountedSubtotal);
        }

        double finalTotal = discountedSubtotal + deliveryCost;
        if (giftWrap != null && giftWrap) {
            finalTotal += 3.0;
            order.setGiftWrap(true);
        }
        if (personalizedMessage != null && !personalizedMessage.isBlank()) {
            order.setPersonalizedMessage(personalizedMessage);
        }

        order.setOrderProducts(orderProducts);
        order.setTotalPrice(finalTotal);
        order = orderRepository.save(order);

        cart.setStatus(CHECKED_OUT);
        cartRepository.save(cart);
        return order;
    }


    @Transactional
    public void deduplicateActiveCarts(Long clientId, String sessionId) {
        if (clientId != null) {
            List<Cart> carts = cartRepository.findAllByClientIdAndStatusOrderByCreatedDateDesc(clientId, ACTIVE);
            for (int i = 1; i < carts.size(); i++) {
                carts.get(i).setStatus("ARCHIVED");
                cartRepository.save(carts.get(i));
            }
        } else if (sessionId != null) {
            List<Cart> carts = cartRepository.findAllBySessionIdAndStatusOrderByCreatedDateDesc(sessionId, ACTIVE);
            for (int i = 1; i < carts.size(); i++) {
                carts.get(i).setStatus("ARCHIVED");
                cartRepository.save(carts.get(i));
            }
        }
    }
}
