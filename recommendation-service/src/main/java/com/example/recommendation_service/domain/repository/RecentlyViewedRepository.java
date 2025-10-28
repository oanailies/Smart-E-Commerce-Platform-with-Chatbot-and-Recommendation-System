package com.example.recommendation_service.domain.repository;

import com.example.recommendation_service.domain.model.RecentlyViewedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewedProduct, Long> {
    List<RecentlyViewedProduct> findTop10ByUserIdOrderByViewedAtDesc(Long userId);
    boolean existsByUserIdAndProductIdAndViewedAtAfter(Long userId, Long productId, LocalDateTime after);
    void deleteByViewedAtBefore(LocalDateTime cutoffDate);
}
