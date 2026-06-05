package ru.adnr.flowmanager.kafka;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.adnr.flowmanager.config.OutboxProperties;
import ru.adnr.flowmanager.entity.OutboxMessage;
import ru.adnr.flowmanager.service.OutboxService;

@Component
public class OutboxKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxKafkaPublisher.class);

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;

    public OutboxKafkaPublisher(
            OutboxService outboxService,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties properties
    ) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publish-fixed-delay-ms:5000}")
    @Transactional
    public void publishPending() {
        List<OutboxMessage> messages = outboxService.findReadyForPublishing(Instant.now(), properties.batchSize());
        for (OutboxMessage message : messages) {
            publish(message);
        }
    }

    private void publish(OutboxMessage message) {
        try {
            var result = kafkaTemplate.send(message.getTopic(), message.getMessageKey(), message.getPayload())
                    .get(properties.sendTimeoutMs(), TimeUnit.MILLISECONDS);
            message.markSent();
            log.info("Published outbox message. outboxId={}, aggregateId={}, topic={}, partition={}, offset={}",
                    message.getId(),
                    message.getAggregateId(),
                    message.getTopic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception exception) {
            message.markFailed(
                    exception.getMessage(),
                    Instant.now().plus(Duration.ofMillis(properties.retryDelayMs())),
                    properties.maxAttempts()
            );
            log.error("Failed to publish outbox message. outboxId={}, aggregateId={}, topic={}, attempts={}",
                    message.getId(),
                    message.getAggregateId(),
                    message.getTopic(),
                    message.getAttempts(),
                    exception);
        }
    }
}
