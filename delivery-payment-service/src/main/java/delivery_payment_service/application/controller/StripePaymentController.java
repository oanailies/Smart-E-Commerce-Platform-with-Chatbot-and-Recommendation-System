package delivery_payment_service.application.controller;

import delivery_payment_service.application.dto.CreateCheckoutRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/payments")
public class StripePaymentController {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;


    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CreateCheckoutRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> params = new HashMap<>();
            params.put("mode", "payment");
            params.put("success_url", "http://localhost:5173/success");
            params.put("cancel_url", "http://localhost:5173/cancel");
            params.put("line_items[0][quantity]", "1");
            params.put("line_items[0][price_data][currency]",
                    request.getCurrency() != null ? request.getCurrency() : "ron");
            params.put("line_items[0][price_data][unit_amount]",
                    String.valueOf((long) (request.getAmount() * 100))); // în bani ×100
            params.put("line_items[0][price_data][product_data][name]",
                    "BeautyShop Order " + request.getOrderId());
            StringBuilder bodyBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (bodyBuilder.length() > 0) bodyBuilder.append("&");
                bodyBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                bodyBuilder.append("=");
                bodyBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            String body = bodyBuilder.toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(STRIPE_SECRET_KEY);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    URI.create("https://api.stripe.com/v1/checkout/sessions"),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(Map.of("id", response.getBody().get("id")));
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


}
