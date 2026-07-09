package ru.adnr.flowmanager.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String login, Throwable cause) {
        super("Subscription not found. login=" + login, cause);
    }
}
