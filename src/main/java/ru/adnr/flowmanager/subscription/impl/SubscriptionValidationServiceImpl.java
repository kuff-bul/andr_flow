package ru.adnr.flowmanager.subscription.impl;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.adnr.flowmanager.config.SubscriptionProperties;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.exception.FileSizeLimitExceededException;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;
import ru.adnr.flowmanager.subscription.SubscriptionValidationService;

@Service
@RequiredArgsConstructor
public class SubscriptionValidationServiceImpl implements SubscriptionValidationService {

    private final SubscriptionCacheService subscriptionCacheService;
    private final SubscriptionProperties subscriptionProperties;
    private final Clock clock;

    private static final String PAID_SUBSCRIPTION = "PAID";

    @Override
    public void validateUploadAllowed(String login, long fileSizeBytes) {
        if (fileSizeBytes <= subscriptionProperties.maxFreeFileSizeBytes()) {
            return;
        }

        SubscriptionResponse subscription = subscriptionCacheService.findByLogin(login);
        if (!canUploadLargeFiles(subscription)) {
            evictExpiredPaidSubscription(subscription);
            throw new FileSizeLimitExceededException(
                    login,
                    fileSizeBytes,
                    subscriptionProperties.maxFreeFileSizeBytes()
            );
        }
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
