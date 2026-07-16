package ru.adnr.flowmanager.subscription.impl;

import org.junit.jupiter.api.Test;
import ru.adnr.flowmanager.config.SubscriptionProperties;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.exception.FileSizeLimitExceededException;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;
import ru.adnr.flowmanager.subscription.SubscriptionValidationService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionValidationServiceImplTest {

    private static final long MAX_FREE_FILE_SIZE_BYTES = 104_857_600L;
    private static final Instant NOW = Instant.parse("2026-07-07T00:00:00Z");

    private final SubscriptionCacheService subscriptionCacheService = mock(SubscriptionCacheService.class);
    private final SubscriptionValidationService subscriptionValidationService = new SubscriptionValidationServiceImpl(
            subscriptionCacheService,
            new SubscriptionProperties(MAX_FREE_FILE_SIZE_BYTES, Duration.ofMinutes(10)),
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void validateUploadAllowedSkipsSubscriptionCheckWhenFileWithinFreeLimit() {
        subscriptionValidationService.validateUploadAllowed("user1", MAX_FREE_FILE_SIZE_BYTES);

        verify(subscriptionCacheService, never()).findByLogin("user1");
    }

    @Test
    void validateUploadAllowedUsesCachedSubscriptionWhenLargeFileIsAllowed() {
        SubscriptionResponse subscription = subscription(true);
        when(subscriptionCacheService.findByLogin("user1")).thenReturn(subscription);

        subscriptionValidationService.validateUploadAllowed("user1", MAX_FREE_FILE_SIZE_BYTES + 1);

        verify(subscriptionCacheService).findByLogin("user1");
    }

    @Test
    void validateUploadAllowedRejectsLargeFileWhenSubscriptionDoesNotAllowIt() {
        when(subscriptionCacheService.findByLogin("user1")).thenReturn(freeSubscription());

        assertThatThrownBy(() -> subscriptionValidationService.validateUploadAllowed("user1", MAX_FREE_FILE_SIZE_BYTES + 1))
                .isInstanceOf(FileSizeLimitExceededException.class)
                .hasMessageContaining("login=user1")
                .hasMessageContaining("fileSizeBytes=104857601");
    }

    @Test
    void validateUploadAllowedRejectsLargeFileWhenCachedPaidSubscriptionExpired() {
        when(subscriptionCacheService.findByLogin("user1")).thenReturn(expiredPaidSubscription());

        assertThatThrownBy(() -> subscriptionValidationService.validateUploadAllowed("user1", MAX_FREE_FILE_SIZE_BYTES + 1))
                .isInstanceOf(FileSizeLimitExceededException.class)
                .hasMessageContaining("login=user1");

        verify(subscriptionCacheService).evict("user1");
    }

    private SubscriptionResponse subscription(boolean canUploadLargeFiles) {
        return paidSubscription(Instant.parse("2026-12-31T23:59:59Z"), canUploadLargeFiles);
    }

    private SubscriptionResponse freeSubscription() {
        return new SubscriptionResponse(
                "user1",
                "FREE",
                null,
                false,
                false
        );
    }

    private SubscriptionResponse expiredPaidSubscription() {
        return paidSubscription(Instant.parse("2026-07-06T23:59:59Z"), true);
    }

    private SubscriptionResponse paidSubscription(Instant expiresAt, boolean canUploadLargeFiles) {
        return new SubscriptionResponse(
                "user1",
                "PAID",
                expiresAt,
                canUploadLargeFiles,
                canUploadLargeFiles
        );
    }
}
