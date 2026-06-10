package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileStatusResponse(
        UUID fileId,
        String originalFileName,
        String status,
        String convertedFilePath,
        String errorMessage
) {
}
