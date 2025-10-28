package product_service.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "favorite_products", uniqueConstraints = @UniqueConstraint(columnNames = {"clientId", "product_id"}))
public class FavoriteProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public FavoriteProduct() {}

    public FavoriteProduct(Long clientId, Product product) {
        this.clientId = clientId;
        this.product = product;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Product getProduct() { return product; }

    public void setId(Long id) { this.id = id; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setProduct(Product product) { this.product = product; }
}
