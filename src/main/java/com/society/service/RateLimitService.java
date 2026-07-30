package com.society.service;

import com.society.config.TwilioProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SECURITY: Rate limiting for OTP endpoints
 * Prevents abuse and brute-force attacks
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final TwilioProperties twilioProperties;

    private Cache<String, RateLimitEntry> requestCache;
    private Cache<String, RateLimitEntry> verifyCache;

    @PostConstruct
    public void init() {
        Duration window = Duration.ofMinutes(twilioProperties.getRateLimitWindowMinutes());

        requestCache = Caffeine.newBuilder()
                .expireAfterWrite(window)
                .maximumSize(50_000)
                .build();

        verifyCache = Caffeine.newBuilder()
                .expireAfterWrite(window)
                .maximumSize(50_000)
                .build();

        log.info("Rate limiter initialized - Window: {}min, Max OTP: {}, Max Verify: {}",
                twilioProperties.getRateLimitWindowMinutes(),
                twilioProperties.getMaxRequestsPerWindow(),
                twilioProperties.getMaxVerifyAttempts());
    }

    /**
     * Check if OTP request is allowed
     */
    public boolean isOtpRequestAllowed(String phoneNo) {
        String key = "req:" + phoneNo;
        RateLimitEntry entry = requestCache.get(key, k -> new RateLimitEntry());

        if (entry.isExpired()) {
            entry.reset();
        }

        int count = entry.count.incrementAndGet();
        return count <= twilioProperties.getMaxRequestsPerWindow();
    }

    /**
     * Check if verify attempt is allowed
     */
    public boolean isVerifyAttemptAllowed(String phoneNo) {
        String key = "verify:" + phoneNo;
        RateLimitEntry entry = verifyCache.get(key, k -> new RateLimitEntry());

        if (entry.isExpired()) {
            entry.reset();
        }

        int count = entry.count.incrementAndGet();
        return count <= twilioProperties.getMaxVerifyAttempts();
    }

    /**
     * Record a failed verify attempt
     */
    public void recordFailedVerify(String phoneNo) {
        String key = "verify:" + phoneNo;
        RateLimitEntry entry = verifyCache.get(key, k -> new RateLimitEntry());
        entry.failedAttempts.incrementAndGet();
    }

    /**
     * Reset rate limit for a phone (after successful login)
     */
    public void resetRateLimit(String phoneNo) {
        requestCache.invalidate("req:" + phoneNo);
        verifyCache.invalidate("verify:" + phoneNo);
    }

    /**
     * Get seconds until rate limit window expires
     */
    public int getRetryAfterSeconds(String phoneNo) {
        String key = "req:" + phoneNo;
        RateLimitEntry entry = requestCache.getIfPresent(key);
        if (entry == null) return 0;

        long elapsed = System.currentTimeMillis() - entry.startTime.get();
        long windowMs = TimeUnit.MINUTES.toMillis(twilioProperties.getRateLimitWindowMinutes());
        long remaining = windowMs - elapsed;

        return (int) Math.max(0, TimeUnit.MILLISECONDS.toSeconds(remaining));
    }

    /**
     * Internal class to track rate limit state
     */
    private static class RateLimitEntry {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger failedAttempts = new AtomicInteger(0);
        final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        boolean isExpired() {
            // Will be handled by Caffeine expiration
            return false;
        }

        void reset() {
            count.set(0);
            failedAttempts.set(0);
            startTime.set(System.currentTimeMillis());
        }
    }
}
