package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    @Value("${rate.limit.window-minutes}")
    private int windowMinutes;
    
    @Value("${rate.limit.max-requests}")
    private int maxRequests;
    
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String clientIP) {
        Bucket bucket = buckets.computeIfAbsent(clientIP, this::createBucket);
        return bucket.tryConsume(1);
    }
    
    private Bucket createBucket(String clientIP) {
        Bandwidth limit = Bandwidth.classic(maxRequests, 
            Refill.intervally(maxRequests, Duration.ofMinutes(windowMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
