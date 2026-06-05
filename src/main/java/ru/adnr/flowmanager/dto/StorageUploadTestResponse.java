package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record StorageUploadTestResponse(
        UUID fileId,
        String objectName,
        boolean exists
) {
}
