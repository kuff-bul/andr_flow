package ru.adnr.flowmanager.dto;

import java.time.Instant;

public record SubscriptionResponse(
        String login,
        String subscriptionType,
        Instant expiresAt,
        boolean paidActive,
        boolean canUploadLargeFiles
) {
}
