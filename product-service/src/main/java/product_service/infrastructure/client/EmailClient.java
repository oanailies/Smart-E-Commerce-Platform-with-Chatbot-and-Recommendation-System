package product_service.infrastructure.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserClient userClient;

    @Value("${email-service.url}")
    private String emailServiceUrl;

    public void sendEmailToClient(Long clientId, String jwtToken, String subject, String message) {
        String to = userClient.getClientEmailById(clientId, jwtToken);
        if (to == null || to.isBlank()) {
            System.err.println("Email address not found for clientId=" + clientId);
            return;
        }
        sendEmail(to, subject, message);
    }

    public void sendEmail(String to, String subject, String message) {
        String url = emailServiceUrl + "/email/send";

        Map<String, String> body = Map.of(
                "to", to,
                "subject", subject,
                "message", message
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendEmailWithPdf(String to, String subject, String message, String fileName, byte[] pdfBytes) {
        String url = emailServiceUrl + "/email/send-pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("to", to);
        body.add("subject", subject);
        body.add("message", message);
        body.add("fileName", fileName);
        body.add("pdfBytes", new org.springframework.core.io.ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send email with PDF to " + to + ": " + e.getMessage());
        }
    }

    public void sendEmailWithPdfToClient(Long clientId, String jwtToken, String subject, String message, String fileName, byte[] pdfBytes) {
        String to = userClient.getClientEmailById(clientId, jwtToken);
        if (to == null || to.isBlank()) {
            System.err.println("Email address not found for clientId=" + clientId);
            return;
        }
        sendEmailWithPdf(to, subject, message, fileName, pdfBytes);
    }
}
