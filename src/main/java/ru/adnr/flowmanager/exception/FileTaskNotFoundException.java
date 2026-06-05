package ru.adnr.flowmanager.exception;

import java.util.UUID;

public class FileTaskNotFoundException extends RuntimeException {

    public FileTaskNotFoundException(UUID id) {
        super("File task not found: " + id);
    }
}
