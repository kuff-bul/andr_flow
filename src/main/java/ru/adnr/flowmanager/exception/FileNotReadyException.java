package ru.adnr.flowmanager.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FileNotReadyException extends RuntimeException {

    public FileNotReadyException(UUID id) {
        super("Converted file is not ready: " + id);
    }
}
