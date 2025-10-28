package com.example.recommendation_service.application.controller;

import com.example.recommendation_service.application.dto.ProductDTO;
import com.example.recommendation_service.application.service.RecommendationService;
import com.example.recommendation_service.infrastructure.client.UserClient;
import com.example.recommendation_service.infrastructure.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {

    private final RecommendationService service;
    private final JwtUtil jwtUtil;
    private final UserClient userClient;

    public RecommendationController(RecommendationService service, JwtUtil jwtUtil, UserClient userClient) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.userClient = userClient;
    }

    @PostMapping("/recently-viewed/{productId}")
    public ResponseEntity<Void> saveRecentlyViewed(@PathVariable Long productId,
                                                   @RequestHeader("Authorization") String jwtToken) {
        Long userId = jwtUtil.extractUserId(jwtToken);
        service.saveViewed(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<List<ProductDTO>> getRecentlyViewed(@RequestHeader("Authorization") String jwtToken) {
        Long userId = jwtUtil.extractUserId(jwtToken);
        return ResponseEntity.ok(service.getRecentlyViewed(userId, jwtToken));
    }

    @GetMapping("/client-id")
    public ResponseEntity<Long> getClientId(@RequestHeader("Authorization") String jwtToken) {
        Long userId = jwtUtil.extractUserId(jwtToken);
        Long clientId = userClient.getClientIdByUserAccountId(userId, jwtToken);
        return ResponseEntity.ok(clientId);
    }

    @GetMapping("/latest-added")
    public ResponseEntity<List<ProductDTO>> getLatestDistinctProducts(@RequestHeader("Authorization") String jwtToken) {
        return ResponseEntity.ok(service.getLatestDistinctProducts(jwtToken));
    }


}
