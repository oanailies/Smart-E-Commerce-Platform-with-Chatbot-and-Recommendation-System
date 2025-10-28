package order_service.application.dto;

public class DeliveryDTO {


    private Long id;
    private Long orderId;
    private Long clientId;
    private Long addressId;
    private String awb;
    private String method;
    private String courierCompany;
    private String easyboxCompany;
    private String easyboxLocation;
    private String pickupLocation;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getAwb() {
        return awb;
    }

    public void setAwb(String awb) {
        this.awb = awb;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
