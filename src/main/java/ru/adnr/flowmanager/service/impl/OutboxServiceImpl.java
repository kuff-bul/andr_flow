package ru.adnr.flowmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.entity.OutboxMessage;
import ru.adnr.flowmanager.exception.KafkaPublishException;
import ru.adnr.flowmanager.repository.OutboxMessageRepository;
import ru.adnr.flowmanager.service.OutboxService;

@Service
public class OutboxServiceImpl implements OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final String conversionRequestTopic;

    public OutboxServiceImpl(
            OutboxMessageRepository outboxMessageRepository,
            ObjectMapper objectMapper,
            @Value("${app.kafka.conversion-request-topic}") String conversionRequestTopic
    ) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.objectMapper = objectMapper;
        this.conversionRequestTopic = conversionRequestTopic;
    }

    @Override
    @Transactional
    public void enqueueFileConversionRequested(FileConversionRequestedEvent event) {
        outboxMessageRepository.save(new OutboxMessage(
                event.fileId(),
                conversionRequestTopic,
                event.fileId().toString(),
                toJson(event)
        ));
    }

    @Override
    @Transactional
    public List<OutboxMessage> findReadyForPublishing(Instant now, int batchSize) {
        return outboxMessageRepository.findReadyForPublishing(now, batchSize);
    }

    private String toJson(FileConversionRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new KafkaPublishException("Failed to serialize file conversion request event", exception);
        }
    }
}
