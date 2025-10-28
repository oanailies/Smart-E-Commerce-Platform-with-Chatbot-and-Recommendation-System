package delivery_payment_service.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "client_addresses")
public class ClientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    private String country;
    private String county;
    private String city;
    private String street;
    private String postalCode;

    public ClientAddress() {}

    public ClientAddress(Long clientId, String country, String county, String city, String street, String postalCode) {
        this.clientId = clientId;
        this.country = country;
        this.county = county;
        this.city = city;
        this.street = street;
        this.postalCode = postalCode;
    }


    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getCountry() { return country; }
    public String getCounty() { return county; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public String getPostalCode() { return postalCode; }

    public void setId(Long id) { this.id = id; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setCountry(String country) { this.country = country; }
    public void setCounty(String county) { this.county = county; }
    public void setCity(String city) { this.city = city; }
    public void setStreet(String street) { this.street = street; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
