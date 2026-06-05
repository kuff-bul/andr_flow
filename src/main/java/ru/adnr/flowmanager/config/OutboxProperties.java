package ru.adnr.flowmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        int batchSize,
        long publishFixedDelayMs,
        long sendTimeoutMs,
        long retryDelayMs,
        int maxAttempts
) {
    public OutboxProperties {
        if (batchSize <= 0) {
            batchSize = 50;
        }
        if (publishFixedDelayMs <= 0) {
            publishFixedDelayMs = 5000;
        }
        if (sendTimeoutMs <= 0) {
            sendTimeoutMs = 10000;
        }
        if (retryDelayMs <= 0) {
            retryDelayMs = 30000;
        }
        if (maxAttempts <= 0) {
            maxAttempts = 10;
        }
    }
}
