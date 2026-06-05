package ru.adnr.flowmanager.kafka;

import ru.adnr.flowmanager.dto.FileConversionResultEvent;

public interface FileConversionResultConsumer {

    void consume(FileConversionResultEvent event);
}
