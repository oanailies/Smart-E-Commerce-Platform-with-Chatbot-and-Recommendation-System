package com.example.recommendation_service.application.service;

import com.example.recommendation_service.domain.repository.RecentlyViewedRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CleanupService {

    private final RecentlyViewedRepository repository;

    public CleanupService(RecentlyViewedRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 15,19 22 * * *")
    @Transactional
    public void cleanupOldViews() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        repository.deleteByViewedAtBefore(cutoffDate);
        System.out.println("Deleted recently viewed products older than 7 days at " + LocalDateTime.now());
    }

}
