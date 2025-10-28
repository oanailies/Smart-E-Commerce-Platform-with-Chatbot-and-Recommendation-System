package product_service.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shades")
public class Shade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = true)
    private String imageUrl;


    public Shade() {}

    public Shade(String name,  String imageUrl) {
        this.name = name;
        this.imageUrl=imageUrl;
    }


    public Long getId() { return id; }
    public String getName() { return name; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
