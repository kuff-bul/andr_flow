package ru.adnr.flowmanager.kafka;

import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;

public interface FileConversionProducer {

    void send(FileConversionRequestedEvent event);
}
