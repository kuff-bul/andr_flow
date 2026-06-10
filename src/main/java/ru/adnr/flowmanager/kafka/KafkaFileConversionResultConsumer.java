package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.adnr.flowmanager.dto.FileConversionCompletedEvent;
import ru.adnr.flowmanager.dto.FileConversionErrorEvent;
import ru.adnr.flowmanager.exception.FileNotFoundException;
import ru.adnr.flowmanager.service.FileTaskService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaFileConversionResultConsumer implements FileConversionResultConsumer {

    private final ObjectMapper objectMapper;
    private final FileTaskService fileTaskService;

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

    @KafkaListener(topics = "${app.kafka.conversion-error-topic}")
    public void listenError(String payload, Acknowledgment acknowledgment) {
        try {
            consume(objectMapper.readValue(payload, FileConversionErrorEvent.class));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse file conversion error event. payload={}", payload, exception);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @Override
    public void consume(FileConversionCompletedEvent event) {
        markSuccess(event);
    }

    @Override
    public void consume(FileConversionErrorEvent event) {
        try {
            fileTaskService.markError(event.fileId(), event.errorMessage());
            log.info("File conversion marked as ERROR. fileId={}, errorMessage={}",
                    event.fileId(), event.errorMessage());
        } catch (FileNotFoundException exception) {
            log.error("File task not found for conversion ERROR event. fileId={}", event.fileId());
        }
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
