package ru.adnr.flowmanager.kafka;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.adnr.flowmanager.dto.FileConversionCompletedEvent;
import ru.adnr.flowmanager.exception.FileNotFoundException;
import ru.adnr.flowmanager.service.FileTaskService;

@ExtendWith(MockitoExtension.class)
class KafkaFileConversionResultConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FileTaskService fileTaskService;

    private KafkaFileConversionResultConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaFileConversionResultConsumer(objectMapper, fileTaskService);
    }

    @Test
    void consume_successEventMarksTaskSuccess() {
        String messageId = "21a8ba86-815b-4002-afd3-86dfb68ccc13";
        consumer.consume(new FileConversionCompletedEvent(
                messageId,
                messageId,
                "files",
                "source/file.txt",
                "files",
                "converted/" + messageId + "/test.pdf",
                "TXT"
        ));

        verify(fileTaskService).markSuccess(
                java.util.UUID.fromString(messageId),
                "files",
                "converted/" + messageId + "/test.pdf"
        );
    }

    @Test
    void consume_missingTaskIsHandledWithoutFailure() {
        String messageId = "365acd68-f47e-46dd-80ab-6910de368351";
        when(fileTaskService.markSuccess(java.util.UUID.fromString(messageId), "files", "converted/test.pdf"))
                .thenThrow(new FileNotFoundException(java.util.UUID.fromString(messageId)));

        consumer.consume(new FileConversionCompletedEvent(
                messageId,
                messageId,
                "files",
                "source/file.txt",
                "files",
                "converted/test.pdf",
                "TXT"
        ));

        verify(fileTaskService).markSuccess(java.util.UUID.fromString(messageId), "files", "converted/test.pdf");
    }
}
