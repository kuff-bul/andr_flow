package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.adnr.flowmanager.dto.FileConversionCompletedEvent;
import ru.adnr.flowmanager.exception.FileNotFoundException;
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
            consume(objectMapper.readValue(payload, FileConversionCompletedEvent.class));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse file conversion result event. payload={}", payload, exception);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @Override
    public void consume(FileConversionCompletedEvent event) {
        markSuccess(event);
    }

    private void markSuccess(FileConversionCompletedEvent event) {
        try {
            UUID fileId = UUID.fromString(event.messageId());
            fileTaskService.markSuccess(fileId, event.pdfBucket(), event.pdfObjectKey());
            log.info("File conversion marked as SUCCESS. fileId={}, pdfBucket={}, pdfObjectKey={}",
                    event.messageId(), event.pdfBucket(), event.pdfObjectKey());
        } catch (FileNotFoundException exception) {
            log.error("File task not found for conversion SUCCESS event. fileId={}", event.messageId());
        } catch (IllegalArgumentException exception) {
            log.error("Invalid messageId in file conversion completed event. messageId={}", event.messageId(), exception);
        }
    }
}
