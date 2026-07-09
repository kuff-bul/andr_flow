package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.adnr.flowmanager.dto.SubscriptionChangedEvent;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SubscriptionEventConsumerTest {

    private final SubscriptionCacheService subscriptionCacheService = mock(SubscriptionCacheService.class);
    private final SubscriptionEventConsumer consumer = new SubscriptionEventConsumer(
            new ObjectMapper(),
            subscriptionCacheService
    );

    @Test
    void consumeEvictsCacheWhenSubscriptionExpired() {
        consumer.consume(new SubscriptionChangedEvent("user1", "SUBSCRIPTION_EXPIRED"));

        verify(subscriptionCacheService).evict("user1");
    }

    @Test
    void consumeIgnoresUnsupportedEvents() {
        consumer.consume(new SubscriptionChangedEvent("user1", "OTHER"));

        verify(subscriptionCacheService, never()).evict("user1");
    }
}
