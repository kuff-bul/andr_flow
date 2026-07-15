package ru.adnr.flowmanager.subscription.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.adnr.flowmanager.config.SubscriptionProperties;
import ru.adnr.flowmanager.dto.SubscriptionResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisSubscriptionCacheServiceTest {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = valueOperations();
    private final RedisSubscriptionCacheService cacheService = new RedisSubscriptionCacheService(
            redisTemplate,
            objectMapper(),
            new SubscriptionProperties(104_857_600L, CACHE_TTL)
    );

    @Test
    void findByLoginReturnsCachedSubscription() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("subscription:user1")).thenReturn(
                "{\"login\":\"user1\",\"subscriptionType\":\"PAID\",\"expiresAt\":\"2026-12-31T23:59:59Z\",\"paidActive\":true,\"canUploadLargeFiles\":true}"
        );

        Optional<SubscriptionResponse> subscription = cacheService.findByLogin("user1");

        assertThat(subscription).isPresent();
        assertThat(subscription.get().login()).isEqualTo("user1");
        assertThat(subscription.get().subscriptionType()).isEqualTo("PAID");
        assertThat(subscription.get().expiresAt()).isEqualTo(Instant.parse("2026-12-31T23:59:59Z"));
    }

    @Test
    void findByLoginReturnsEmptyWhenCacheMisses() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("subscription:user1")).thenReturn(null);

        Optional<SubscriptionResponse> subscription = cacheService.findByLogin("user1");

        assertThat(subscription).isEmpty();
    }

    @Test
    void saveStoresSubscriptionWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SubscriptionResponse subscription = new SubscriptionResponse(
                "user1",
                "PAID",
                Instant.parse("2026-12-31T23:59:59Z"),
                true,
                true
        );

        cacheService.save(subscription);

        verify(valueOperations).set(
                "subscription:user1",
                "{\"login\":\"user1\",\"subscriptionType\":\"PAID\",\"expiresAt\":\"2026-12-31T23:59:59Z\",\"paidActive\":true,\"canUploadLargeFiles\":true}",
                CACHE_TTL
        );
    }

    @Test
    void evictDeletesSubscriptionKey() {
        cacheService.evict("user1");

        verify(redisTemplate).delete("subscription:user1");
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations() {
        return mock(ValueOperations.class);
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
