package ru.adnr.flowmanager.dto;

public record FileConversionCompletedEvent(
        String messageId,
        String correlationId,
        String sourceBucket,
        String sourceObjectKey,
        String pdfBucket,
        String pdfObjectKey,
        String fileType
) {
}
