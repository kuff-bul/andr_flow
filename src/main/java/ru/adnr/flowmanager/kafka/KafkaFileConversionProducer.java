package ru.adnr.flowmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.exception.KafkaPublishException;

@Component
public class KafkaFileConversionProducer implements FileConversionProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaFileConversionProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaFileConversionProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.conversion-request-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void send(FileConversionRequestedEvent event) {
        String key = event.fileId().toString();
        String payload = toJson(event);

        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send file conversion request event. fileId={}, topic={}",
                                event.fileId(), topic, exception);
                        return;
                    }
                    log.info("Sent file conversion request event. fileId={}, topic={}, partition={}, offset={}",
                            event.fileId(),
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }

    private String toJson(FileConversionRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new KafkaPublishException("Failed to serialize file conversion request event", exception);
        }
    }
}
