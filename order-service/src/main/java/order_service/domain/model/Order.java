package order_service.domain.model;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm", timezone = "Europe/Bucharest")
    private Date orderDate;
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    private Boolean giftWrap = false;

    @Column(length = 500)
    private String personalizedMessage;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();


    public Order() {}

    public Order(Long clientId, Date orderDate, String orderStatus, List<OrderProduct> orderProducts) {
        this.clientId = clientId;
        this.orderDate = orderDate;
        this.orderProducts = orderProducts;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Date getOrderDate() { return orderDate; }

    public List<OrderProduct> getOrderProducts() { return orderProducts; }

    public void setId(Long id) { this.id = id; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public void setOrderProducts(List<OrderProduct> orderProducts) { this.orderProducts = orderProducts; }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Boolean getGiftWrap() { return giftWrap; }
    public void setGiftWrap(Boolean giftWrap) { this.giftWrap = giftWrap; }

    public String getPersonalizedMessage() { return personalizedMessage; }
    public void setPersonalizedMessage(String personalizedMessage) { this.personalizedMessage = personalizedMessage; }

}
