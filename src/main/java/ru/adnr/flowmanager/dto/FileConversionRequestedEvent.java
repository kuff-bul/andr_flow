package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileConversionRequestedEvent(
        UUID fileId,
        String originalFileName,
        String sourceBucket,
        String sourceObjectName
) {
}
