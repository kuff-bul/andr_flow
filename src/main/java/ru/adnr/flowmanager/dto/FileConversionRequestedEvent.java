package ru.adnr.flowmanager.dto;

public record FileConversionRequestedEvent(
        String messageId,
        String bucket,
        String objectKey,
        String fileType,
        String originalFileName
) {
}
