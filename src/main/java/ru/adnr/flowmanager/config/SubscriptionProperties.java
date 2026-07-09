package ru.adnr.flowmanager.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.subscription")
public record SubscriptionProperties(
        @Positive long maxFreeFileSizeBytes,
        Duration cacheTtl
) {
}
