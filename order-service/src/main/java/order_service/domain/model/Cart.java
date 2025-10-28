package order_service.domain.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;
    private String sessionId;
    private Date createdDate;
    private String status;


    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long clientId) {
        this.clientId = clientId;
        this.createdDate = new Date();
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Date getCreatedDate() { return createdDate; }
    public String getStatus() { return status; }
    public List<CartItem> getCartItems() { return cartItems; }

    public void setId(Long id) { this.id = id; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public void setStatus(String status) { this.status = status; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

}
