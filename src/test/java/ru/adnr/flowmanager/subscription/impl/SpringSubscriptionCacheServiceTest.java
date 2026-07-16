package ru.adnr.flowmanager.subscription.impl;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import ru.adnr.flowmanager.client.SubscriptionClient;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.exception.SubscriptionCheckException;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringSubscriptionCacheServiceTest {

    private final SubscriptionClient subscriptionClient = mock(SubscriptionClient.class);
    private final SpringSubscriptionCacheService cacheService = new SpringSubscriptionCacheService(subscriptionClient);

    @Test
    void findByLoginLoadsSubscriptionThroughClient() throws Exception {
        SubscriptionResponse expected = subscription();
        when(subscriptionClient.getSubscription("user1")).thenReturn(expected);

        assertThat(cacheService.findByLogin("user1")).isEqualTo(expected);
        verify(subscriptionClient).getSubscription("user1");

        Method method = SpringSubscriptionCacheService.class.getMethod("findByLogin", String.class);
        assertThat(method.getAnnotation(Cacheable.class).cacheNames()).containsExactly("subscriptions");
    }

    @Test
    void findByLoginWrapsClientFailure() {
        when(subscriptionClient.getSubscription("user1"))
                .thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThatThrownBy(() -> cacheService.findByLogin("user1"))
                .isInstanceOf(SubscriptionCheckException.class)
                .hasMessageContaining("login=user1");
    }

    @Test
    void evictUsesCacheEvictAnnotation() throws Exception {
        Method method = SpringSubscriptionCacheService.class.getMethod("evict", String.class);

        assertThat(method.getAnnotation(CacheEvict.class).cacheNames()).containsExactly("subscriptions");
        assertThat(method.getAnnotation(CacheEvict.class).key()).isEqualTo("#login");
    }

    private SubscriptionResponse subscription() {
        return new SubscriptionResponse(
                "user1",
                "PAID",
                Instant.parse("2026-12-31T23:59:59Z"),
                true,
                true
        );
    }
}
