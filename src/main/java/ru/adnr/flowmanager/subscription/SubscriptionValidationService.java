package ru.adnr.flowmanager.subscription;

public interface SubscriptionValidationService {

    void validateUploadAllowed(String login, long fileSizeBytes);
}
