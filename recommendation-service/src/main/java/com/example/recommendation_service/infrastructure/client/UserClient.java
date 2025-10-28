package com.example.recommendation_service.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long getClientIdByUserAccountId(Long userAccountId, String jwtToken) {
        String url = authServiceUrl + "/users/" + userAccountId + "/clientId";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Long.class);
        return response.getBody();
    }
}
