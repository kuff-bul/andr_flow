package ru.adnr.flowmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.adnr.flowmanager.config.KafkaProperties;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.entity.OutboxMessage;
import ru.adnr.flowmanager.exception.KafkaPublishException;
import ru.adnr.flowmanager.repository.OutboxMessageRepository;
import ru.adnr.flowmanager.service.OutboxService;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final KafkaProperties kafkaProperties;

    @Override
    @Transactional
    public void enqueueFileConversionRequested(FileConversionRequestedEvent event) {
        outboxMessageRepository.save(new OutboxMessage(
                UUID.fromString(event.messageId()),
                kafkaProperties.conversionRequestTopic(),
                event.messageId(),
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
