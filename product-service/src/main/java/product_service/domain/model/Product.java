package product_service.domain.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;

    @Column(name = "on_sale", nullable = false)
    private boolean onSale = false;

    @Column(name = "discounted_price")
    private Double discountedPrice;


    @Column(name = "discount_percent")
    private Double discountPercent;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "shade_id")
    private Shade shade;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    public Product() {}

    public Product(String name, String description, double price, int stock,
                   Category category, Brand brand, Gender gender,
                   Size size, Color color, Shade shade) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.gender = gender;
        this.size = size;
        this.color = color;
        this.shade = shade;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isOnSale() { return onSale; }
    public Double getDiscountPercent() { return discountPercent; }
    public Category getCategory() { return category; }
    public Brand getBrand() { return brand; }
    public Gender getGender() { return gender; }
    public Size getSize() { return size; }
    public Color getColor() { return color; }
    public Shade getShade() { return shade; }
    public List<ProductImage> getImages() { return images; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setOnSale(boolean onSale) { this.onSale = onSale; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }
    public void setCategory(Category category) { this.category = category; }
    public void setBrand(Brand brand) { this.brand = brand; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setSize(Size size) { this.size = size; }
    public void setColor(Color color) { this.color = color; }
    public void setShade(Shade shade) { this.shade = shade; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }
    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }


}
