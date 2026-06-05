package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.adnr.flowmanager.dto.FileConversionResultEvent;
import ru.adnr.flowmanager.entity.FileStatus;
import ru.adnr.flowmanager.exception.FileTaskNotFoundException;
import ru.adnr.flowmanager.service.FileTaskService;

@Component
public class KafkaFileConversionResultConsumer implements FileConversionResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaFileConversionResultConsumer.class);

    private final ObjectMapper objectMapper;
    private final FileTaskService fileTaskService;

    public KafkaFileConversionResultConsumer(ObjectMapper objectMapper, FileTaskService fileTaskService) {
        this.objectMapper = objectMapper;
        this.fileTaskService = fileTaskService;
    }

    @KafkaListener(topics = "${app.kafka.conversion-result-topic}")
    public void listen(String payload, Acknowledgment acknowledgment) {
        try {
            consume(objectMapper.readValue(payload, FileConversionResultEvent.class));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse file conversion result event. payload={}", payload, exception);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @Override
    public void consume(FileConversionResultEvent event) {
        if (FileStatus.SUCCESS.name().equals(event.status())) {
            markSuccess(event);
            return;
        }
        if (FileStatus.ERROR.name().equals(event.status())) {
            markError(event);
            return;
        }
        log.error("Unknown file conversion result status. fileId={}, status={}", event.fileId(), event.status());
    }

    private void markSuccess(FileConversionResultEvent event) {
        try {
            fileTaskService.markSuccess(event.fileId(), event.convertedObjectName());
            log.info("File conversion marked as SUCCESS. fileId={}, convertedBucket={}, convertedObjectName={}",
                    event.fileId(), event.convertedBucket(), event.convertedObjectName());
        } catch (FileTaskNotFoundException exception) {
            log.error("File task not found for conversion SUCCESS event. fileId={}", event.fileId());
        }
    }

    private void markError(FileConversionResultEvent event) {
        try {
            fileTaskService.markError(event.fileId(), event.errorMessage());
            log.info("File conversion marked as ERROR. fileId={}", event.fileId());
        } catch (FileTaskNotFoundException exception) {
            log.error("File task not found for conversion ERROR event. fileId={}", event.fileId());
        }
    }
}
