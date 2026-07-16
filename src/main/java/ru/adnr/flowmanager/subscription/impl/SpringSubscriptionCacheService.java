package ru.adnr.flowmanager.subscription.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.adnr.flowmanager.client.SubscriptionClient;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.exception.SubscriptionCheckException;
import ru.adnr.flowmanager.exception.SubscriptionNotFoundException;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;

@Service
@RequiredArgsConstructor
public class SpringSubscriptionCacheService implements SubscriptionCacheService {

    static final String SUBSCRIPTIONS_CACHE = "subscriptions";

    private final SubscriptionClient subscriptionClient;

    @Override
    @Cacheable(cacheNames = SUBSCRIPTIONS_CACHE, key = "#login", sync = true)
    public SubscriptionResponse findByLogin(String login) {
        try {
            return subscriptionClient.getSubscription(login);
        } catch (FeignException.NotFound exception) {
            throw new SubscriptionNotFoundException(login, exception);
        } catch (FeignException exception) {
            throw new SubscriptionCheckException(login, exception);
        }
    }

    @Override
    @CacheEvict(cacheNames = SUBSCRIPTIONS_CACHE, key = "#login")
    public void evict(String login) {
        // Cache eviction is handled by Spring's cache interceptor.
    }
}
