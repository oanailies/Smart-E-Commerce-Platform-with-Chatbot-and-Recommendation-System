package review_service.infrastructure.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public boolean checkClientExists(Long clientId, String jwtToken) {
        String url = authServiceUrl + "/clients/" + clientId;

        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null && !jwtToken.startsWith("Bearer ")) {
            headers.set("Authorization", "Bearer " + jwtToken.trim());
        } else {
            headers.set("Authorization", jwtToken);
        }
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized: Invalid JWT token for client {}", clientId);
            return false;
        } catch (HttpClientErrorException.Forbidden e) {
            logger.error("Forbidden: JWT rejected for client {}", clientId);
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Client not found: {}", clientId);
            return false;
        } catch (Exception e) {
            logger.error("Error while calling auth-service: {}", e.getMessage());
            return false;
        }
    }



    public Long getClientIdByUserAccountId(Long userAccountId, String jwtToken) {
        String url = authServiceUrl + "/users/" + userAccountId + "/clientId"; // Endpoint pentru clientId

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Long> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Long.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("User account not found: {}", userAccountId);
            throw new RuntimeException("User account not found");
        } catch (Exception e) {
            logger.error("Error while retrieving clientId: {}", e.getMessage());
            throw new RuntimeException("Error retrieving clientId");
        }
    }

    public String getClientNameById(Long clientId) {
        String url = authServiceUrl + "/clients/" + clientId + "/name";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching client name for id {}: {}", clientId, e.getMessage());
            return "Anonymous";
        }
    }

    public String getClientEmailById(Long clientId, String jwtToken) {
        String url = authServiceUrl + "/clients/" + clientId + "/email";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching client email for id {}: {}", clientId, e.getMessage());
            return "no-reply@example.com";
        }
    }
}
