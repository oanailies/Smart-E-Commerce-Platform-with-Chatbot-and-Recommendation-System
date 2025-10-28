package com.example.recommendation_service.infrastructure.client;

import com.example.recommendation_service.application.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestTemplate restTemplate;

    @Value("${product-service.url}")
    private String productServiceUrl;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductDTO getFullProductById(Long productId, String jwtToken) {
        String url = productServiceUrl + "/products/" + productId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ProductDTO.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Produsul cu id {} nu a fost gasit in product-service", productId);
            return null;
        } catch (HttpClientErrorException e) {
            log.error("Eroare la apelarea product-service pentru id {}: {}", productId, e.getMessage());
            throw e;
        }
    }

    public ProductDTO[] getAllProducts(String jwtToken) {
        String url = productServiceUrl + "/products";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductDTO[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ProductDTO[].class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Eroare la apelarea product-service pentru getAllProducts: {}", e.getMessage());
            return new ProductDTO[0];
        }
    }
}
