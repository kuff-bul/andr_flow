package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record FileConversionResultEvent(
        UUID fileId,
        String status,
        String convertedBucket,
        String convertedObjectName,
        String errorMessage
) {
}
