package ru.adnr.flowmanager.subscription.impl;

import feign.FeignException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.adnr.flowmanager.client.SubscriptionClient;
import ru.adnr.flowmanager.config.SubscriptionProperties;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.exception.FileSizeLimitExceededException;
import ru.adnr.flowmanager.exception.SubscriptionCheckException;
import ru.adnr.flowmanager.exception.SubscriptionNotFoundException;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;
import ru.adnr.flowmanager.subscription.SubscriptionValidationService;

@Service
@RequiredArgsConstructor
public class SubscriptionValidationServiceImpl implements SubscriptionValidationService {

    private final SubscriptionClient subscriptionClient;
    private final SubscriptionCacheService subscriptionCacheService;
    private final SubscriptionProperties subscriptionProperties;
    private final Clock clock;

    private static final String PAID_SUBSCRIPTION = "PAID";

    @Override
    public void validateUploadAllowed(String login, long fileSizeBytes) {
        if (fileSizeBytes <= subscriptionProperties.maxFreeFileSizeBytes()) {
            return;
        }

        SubscriptionResponse subscription = subscriptionCacheService.findByLogin(login)
                .orElseGet(() -> fetchAndCache(login));
        if (!canUploadLargeFiles(subscription)) {
            evictExpiredPaidSubscription(subscription);
            throw new FileSizeLimitExceededException(
                    login,
                    fileSizeBytes,
                    subscriptionProperties.maxFreeFileSizeBytes()
            );
        }
    }

    private SubscriptionResponse fetchAndCache(String login) {
        SubscriptionResponse subscription;
        try {
            subscription = subscriptionClient.getSubscription(login);
        } catch (FeignException.NotFound exception) {
            throw new SubscriptionNotFoundException(login, exception);
        } catch (FeignException exception) {
            throw new SubscriptionCheckException(login, exception);
        }
        subscriptionCacheService.save(subscription);
        return subscription;
    }

    private boolean canUploadLargeFiles(SubscriptionResponse subscription) {
        Instant expiresAt = subscription.expiresAt();
        return PAID_SUBSCRIPTION.equals(subscription.subscriptionType())
                && expiresAt != null
                && expiresAt.isAfter(Instant.now(clock));
    }

    private void evictExpiredPaidSubscription(SubscriptionResponse subscription) {
        Instant expiresAt = subscription.expiresAt();
        if (PAID_SUBSCRIPTION.equals(subscription.subscriptionType())
                && expiresAt != null
                && !expiresAt.isAfter(Instant.now(clock))) {
            subscriptionCacheService.evict(subscription.login());
        }
    }
}
