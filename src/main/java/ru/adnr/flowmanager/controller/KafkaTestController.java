package ru.adnr.flowmanager.controller;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.dto.KafkaPublishTestResponse;
import ru.adnr.flowmanager.kafka.FileConversionProducer;

@RestController
@RequestMapping("/api/test/kafka")
public class KafkaTestController {

    private final FileConversionProducer fileConversionProducer;
    private final String topic;

    public KafkaTestController(
            FileConversionProducer fileConversionProducer,
            @Value("${app.kafka.conversion-request-topic}") String topic
    ) {
        this.fileConversionProducer = fileConversionProducer;
        this.topic = topic;
    }

    @PostMapping("/conversion-request")
    public KafkaPublishTestResponse sendConversionRequest(
            @RequestParam(defaultValue = "test.txt") String originalFileName,
            @RequestParam(defaultValue = "documents") String sourceBucket,
            @RequestParam(defaultValue = "source/test/test.txt") String sourceObjectName
    ) {
        UUID fileId = UUID.randomUUID();
        FileConversionRequestedEvent event = new FileConversionRequestedEvent(
                fileId,
                originalFileName,
                sourceBucket,
                sourceObjectName
        );
        fileConversionProducer.send(event);
        return new KafkaPublishTestResponse(fileId, topic, fileId.toString(), event);
    }
}
