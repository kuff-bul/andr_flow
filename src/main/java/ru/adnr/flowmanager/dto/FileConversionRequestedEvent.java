package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileConversionRequestedEvent(
        String messageId,
        String bucket,
        String objectKey,
        String fileType,
        String originalFileName
) {
}
