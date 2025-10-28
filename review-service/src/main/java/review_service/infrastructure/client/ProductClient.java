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
import review_service.application.dto.ProductDTO;

@Service
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${product-service.url}")
    private String productServiceUrl;

    public boolean checkProductExists(Long productId, String jwtToken) {
        String url = productServiceUrl + "/products/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found: {}", productId);
            return false;
        } catch (Exception e) {
            logger.error("Error while calling product-service: {}", e.getMessage());
            return false;
        }
    }

    public double getProductPriceById(Long productId, String jwtToken) {
        String url = productServiceUrl + "/products/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ProductDTO.class);
            return response.getBody().getPrice();
        } catch (Exception e) {
            logger.error("Error fetching product price for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Could not retrieve product price from product-service");
        }
    }


    public ProductDTO getFullProductById(Long productId, String jwtToken) {
        String url = productServiceUrl + "/products/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ProductDTO.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching full product for productId {}: {}", productId, e.getMessage());
            throw new RuntimeException("Could not retrieve product data");
        }
    }

    public boolean decreaseStock(Long productId, int quantity, String jwtToken) {
        String url = productServiceUrl + "/products/" + productId + "/decrease-stock?quantity=" + quantity;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            return true;
        } catch (Exception e) {
            logger.error("Failed to decrease stock for product {}: {}", productId, e.getMessage());
            return false;
        }
    }





}
