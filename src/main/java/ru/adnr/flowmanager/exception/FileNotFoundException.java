package ru.adnr.flowmanager.exception;

import java.util.UUID;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(UUID id) {
        super("File task not found: " + id);
    }
}
