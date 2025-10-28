package com.example.recommendation_service.application.service;

import com.example.recommendation_service.application.dto.ProductDTO;
import com.example.recommendation_service.domain.model.RecentlyViewedProduct;
import com.example.recommendation_service.domain.repository.RecentlyViewedRepository;
import com.example.recommendation_service.infrastructure.client.ProductClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecentlyViewedRepository repository;
    private final ProductClient productClient;

    public RecommendationService(RecentlyViewedRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    public void saveViewed(Long userId, Long productId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(60);

        boolean alreadyViewedRecently = repository.existsByUserIdAndProductIdAndViewedAtAfter(
                userId, productId, oneMinuteAgo
        );

        if (!alreadyViewedRecently) {
            repository.save(new RecentlyViewedProduct(userId, productId));
        }
    }

    public List<ProductDTO> getRecentlyViewed(Long userId, String jwtToken) {
        return repository.findTop10ByUserIdOrderByViewedAtDesc(userId).stream()
                .collect(Collectors.toMap(
                        RecentlyViewedProduct::getProductId,
                        v -> v,
                        (existing, replacement) -> existing  
                ))
                .values()
                .stream()
                .sorted((a, b) -> b.getViewedAt().compareTo(a.getViewedAt()))
                .map(v -> productClient.getFullProductById(v.getProductId(), jwtToken))
                .limit(10)
                .collect(Collectors.toList());
    }


    public List<ProductDTO> getLatestDistinctProducts(String jwtToken) {
        ProductDTO[] all = productClient.getAllProducts(jwtToken);

        List<ProductDTO> reversed = new ArrayList<>(List.of(all));
        Collections.reverse(reversed);

        LinkedHashMap<String, ProductDTO> distinctByName = new LinkedHashMap<>();
        for (ProductDTO product : reversed) {
            if (!distinctByName.containsKey(product.getName())) {
                distinctByName.put(product.getName(), product);
            }
            if (distinctByName.size() == 5) break;
        }

        return new ArrayList<>(distinctByName.values());
    }


}