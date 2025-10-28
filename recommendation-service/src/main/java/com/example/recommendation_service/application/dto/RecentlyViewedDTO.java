package com.example.recommendation_service.application.dto;

public class RecentlyViewedDTO {
    private Long userId;
    private Long productId;

    public RecentlyViewedDTO() {}

    public RecentlyViewedDTO(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }


}
