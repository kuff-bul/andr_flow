package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileUploadResponse(
        UUID fileId,
        String status
) {
}
