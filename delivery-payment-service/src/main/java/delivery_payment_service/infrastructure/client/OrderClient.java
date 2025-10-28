package delivery_payment_service.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderClient {

    private final RestTemplate restTemplate;
    private final String ordersServiceUrl;

    public OrderClient(RestTemplate restTemplate,
                       @Value("${order-service.url}") String ordersServiceUrl) {
        this.restTemplate = restTemplate;
        this.ordersServiceUrl = ordersServiceUrl;
    }


    public boolean checkOrderExists(Long orderId, String jwtToken) {

        String url = ordersServiceUrl + "/orders/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Void.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {

            System.out.println("Order check failed: " + e.getMessage());
            return false;
        }
    }

   
}
