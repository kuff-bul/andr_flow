package ru.adnr.flowmanager.dto;

public record SubscriptionChangedEvent(
        String login,
        String reason
) {
}
