package ru.adnr.flowmanager.exception;

public class SubscriptionCheckException extends RuntimeException {

    public SubscriptionCheckException(String login, Throwable cause) {
        super("Failed to check subscription. login=" + login, cause);
    }
}
