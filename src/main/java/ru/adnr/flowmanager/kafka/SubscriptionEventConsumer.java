package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.adnr.flowmanager.dto.SubscriptionChangedEvent;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventConsumer {

    private static final String SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";

    private final ObjectMapper objectMapper;
    private final SubscriptionCacheService subscriptionCacheService;

    @KafkaListener(topics = "${app.kafka.subscription-events-topic}")
    public void listen(String payload, Acknowledgment acknowledgment) {
        try {
            consume(objectMapper.readValue(payload, SubscriptionChangedEvent.class));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse subscription event. payload={}", payload, exception);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    public void consume(SubscriptionChangedEvent event) {
        if (!SUBSCRIPTION_EXPIRED.equals(event.reason())) {
            log.warn("Ignoring unsupported subscription event. login={}, reason={}", event.login(), event.reason());
            return;
        }

        subscriptionCacheService.evict(event.login());
        log.info("Subscription cache evicted. login={}, reason={}", event.login(), event.reason());
    }
}
