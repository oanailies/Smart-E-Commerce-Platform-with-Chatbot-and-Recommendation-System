package delivery_payment_service.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long clientId;
    private Long addressId;
    private String awb;

    private String method;



    @Column(nullable = true)
    private String courierCompany;

    @Column(nullable = true)
    private String easyboxCompany;

    @Column(nullable = true)
    private String easyboxLocation;

    @Column(nullable = true)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    public Delivery() {}

    public Delivery(Long orderId, Long clientId, Long addressId, String method,DeliveryStatus status) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.addressId = addressId;
        this.method = method;
        this.status = status;
    }

    public Delivery(Long orderId, Long clientId, Long addressId, String awb,
                    String method, Double cost, DeliveryStatus status,
                    String courierCompany, String easyboxCompany,
                    String easyboxLocation, String pickupLocation) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.addressId = addressId;
        this.awb = awb;
        this.method = method;
        this.status = status;
        this.courierCompany = courierCompany;
        this.easyboxCompany = easyboxCompany;
        this.easyboxLocation = easyboxLocation;
        this.pickupLocation = pickupLocation;
    }


    public String getAwb() { return awb; }
    public void setAwb(String awb) { this.awb = awb; }

    public Long getId() {
        return id;
    }



    public Long getOrderId() {
        return orderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getMethod() {
        return method;
    }


    public DeliveryStatus getStatus() {
        return status;
    }

    public String getCourierCompany() {
        return courierCompany;
    }

    public void setCourierCompany(String courierCompany) {
        this.courierCompany = courierCompany;
    }

    public String getEasyboxCompany() {
        return easyboxCompany;
    }

    public void setEasyboxCompany(String easyboxCompany) {
        this.easyboxCompany = easyboxCompany;
    }

    public String getEasyboxLocation() {
        return easyboxLocation;
    }

    public void setEasyboxLocation(String easyboxLocation) {
        this.easyboxLocation = easyboxLocation;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}
