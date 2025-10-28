package delivery_payment_service.application.service;

import delivery_payment_service.domain.model.Delivery;
import delivery_payment_service.domain.model.DeliveryStatus;
import delivery_payment_service.domain.repository.DeliveryRepository;
import delivery_payment_service.infrastructure.client.OrderClient;
import delivery_payment_service.infrastructure.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserClient userClient;
    private final OrderClient orderClient;

    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository, UserClient userClient, OrderClient orderClient) {
        this.deliveryRepository = deliveryRepository;
        this.userClient = userClient;
        this.orderClient = orderClient;
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    public Delivery createDelivery(Delivery delivery, String jwtToken) {
        boolean orderExists = orderClient.checkOrderExists(delivery.getOrderId(), jwtToken);
        if (!orderExists) {
            throw new RuntimeException("Order does not exist!");
        }

        boolean clientExists = userClient.checkClientExists(delivery.getClientId(), jwtToken);
        if (!clientExists) {
            throw new RuntimeException("Client does not exist or token is invalid!");
        }

        validateAndNormalizeByMethod(delivery);

        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setAwb(generateFakeAwb());

        return deliveryRepository.save(delivery);
    }

    private String generateFakeAwb() {
        return "AWB-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    public Delivery updateDelivery(Long id, Delivery updatedDelivery) {
        Delivery existing = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found!"));

        existing.setMethod(updatedDelivery.getMethod());
        existing.setStatus(updatedDelivery.getStatus());
        existing.setAddressId(updatedDelivery.getAddressId());
        existing.setCourierCompany(updatedDelivery.getCourierCompany());
        existing.setEasyboxCompany(updatedDelivery.getEasyboxCompany());
        existing.setEasyboxLocation(updatedDelivery.getEasyboxLocation());
        existing.setPickupLocation(updatedDelivery.getPickupLocation());

        validateAndNormalizeByMethod(existing);

        return deliveryRepository.save(existing);
    }

    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }

    private void validateAndNormalizeByMethod(Delivery d) {
        if (d.getMethod() == null) {
            throw new RuntimeException("Delivery method is required!");
        }
        String method = d.getMethod();

        switch (method) {
            case "Courier":
                if (d.getAddressId() == null) {
                    throw new RuntimeException("AddressId is required for Courier deliveries.");
                }
                if (isBlank(d.getCourierCompany())) {
                    throw new RuntimeException("courierCompany is required for Courier deliveries.");
                }
                d.setEasyboxCompany(null);
                d.setEasyboxLocation(null);
                d.setPickupLocation(null);
                break;

            case "Easybox":
                if (isBlank(d.getEasyboxCompany())) {
                    throw new RuntimeException("easyboxCompany is required for Easybox deliveries.");
                }
                if (isBlank(d.getEasyboxLocation())) {
                    throw new RuntimeException("easyboxLocation is required for Easybox deliveries.");
                }
                d.setCourierCompany(null);
                d.setPickupLocation(null);
                break;

            case "PickupStore":
                if (isBlank(d.getPickupLocation())) {
                    throw new RuntimeException("pickupLocation is required for PickupStore deliveries.");
                }
                d.setCourierCompany(null);
                d.setEasyboxCompany(null);
                d.setEasyboxLocation(null);
                break;

            default:
                throw new RuntimeException("Unknown delivery method: " + method);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public Delivery getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId).orElse(null);
    }

}
