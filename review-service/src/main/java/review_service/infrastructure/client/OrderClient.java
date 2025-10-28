package review_service.infrastructure.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review_service.application.dto.OrderDTO;

@Service
public class OrderClient {

    private static final Logger logger = LoggerFactory.getLogger(OrderClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${order-service.url}")
    private String orderServiceUrl;

    public OrderDTO getOrderById(Long orderId, String jwtToken) {
        String url = orderServiceUrl + "/orders/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, OrderDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Order not found: {}", orderId);
            throw new RuntimeException("Order not found: " + orderId);
        } catch (Exception e) {
            logger.error("Error calling order-service: {}", e.getMessage());
            throw new RuntimeException("Could not retrieve order " + orderId);
        }
    }
}
