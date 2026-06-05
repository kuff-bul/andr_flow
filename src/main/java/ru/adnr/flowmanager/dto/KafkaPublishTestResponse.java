package ru.adnr.flowmanager.dto;

import java.util.UUID;

public record KafkaPublishTestResponse(
        UUID fileId,
        String topic,
        String messageKey,
        FileConversionRequestedEvent event
) {
}
