package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileConversionErrorEvent(
        UUID fileId,
        String errorMessage
) {
}
