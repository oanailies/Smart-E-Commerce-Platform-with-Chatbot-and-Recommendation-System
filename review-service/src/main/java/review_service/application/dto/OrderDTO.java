package review_service.application.dto;

import java.util.List;

public class OrderDTO {
    private Long id;
    private Long clientId;
    private List<OrderProductDTO> orderProducts;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public List<OrderProductDTO> getOrderProducts() { return orderProducts; }
    public void setOrderProducts(List<OrderProductDTO> orderProducts) { this.orderProducts = orderProducts; }
}
