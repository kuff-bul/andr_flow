package ru.adnr.flowmanager.exception;

public class MissingUserLoginException extends RuntimeException {

    public MissingUserLoginException() {
        super("X-User-Login header is required");
    }
}
