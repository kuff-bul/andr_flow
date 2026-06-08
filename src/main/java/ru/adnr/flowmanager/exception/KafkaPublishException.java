package ru.adnr.flowmanager.exception;

import org.springframework.kafka.KafkaException;

public class KafkaPublishException extends KafkaException {

    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
