package ru.adnr.flowmanager.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        @NotBlank String conversionRequestTopic,
        @NotBlank String subscriptionEventsTopic
) {
}
