package order_service.infrastructure.client;

import order_service.application.dto.ClientAddressDTO;
import order_service.application.dto.DeliveryDTO;
import order_service.application.dto.PaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class DeliveryPaymentClient {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryPaymentClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${delivery-payment-service.url}")
    private String deliveryPaymentServiceUrl;

    public DeliveryDTO getDeliveryByOrderId(Long orderId, String jwtToken) {
        String url = deliveryPaymentServiceUrl + "/deliveries/order/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, DeliveryDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("No delivery found for order {}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching delivery for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Could not retrieve delivery info");
        }
    }

    public ClientAddressDTO getAddressById(Long addressId, String jwtToken) {
        String url = deliveryPaymentServiceUrl + "/addresses/" + addressId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, ClientAddressDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Address not found with id {}", addressId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching address {}: {}", addressId, e.getMessage());
            throw new RuntimeException("Could not retrieve address info");
        }
    }

    public PaymentDTO getPaymentById(Long paymentId, String jwtToken) {
        String url = deliveryPaymentServiceUrl + "/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, PaymentDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Payment not found with id {}", paymentId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching payment {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Could not retrieve payment info");
        }
    }
}
