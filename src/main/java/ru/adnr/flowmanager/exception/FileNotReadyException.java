package ru.adnr.flowmanager.exception;

import java.util.UUID;

public class FileNotReadyException extends RuntimeException {

    public FileNotReadyException(UUID id) {
        super("Converted file is not ready yet");
    }
}
