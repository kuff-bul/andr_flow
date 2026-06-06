package ru.adnr.flowmanager.exception;

public class EmptyFileException extends RuntimeException {

    public EmptyFileException() {
        super("File must not be empty");
    }
}
