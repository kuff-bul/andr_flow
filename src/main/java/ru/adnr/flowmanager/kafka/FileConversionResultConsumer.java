package ru.adnr.flowmanager.kafka;

import ru.adnr.flowmanager.dto.FileConversionCompletedEvent;

public interface FileConversionResultConsumer {

    void consume(FileConversionCompletedEvent event);
}
