package ru.adnr.flowmanager.kafka;

import ru.adnr.flowmanager.dto.FileConversionCompletedEvent;
import ru.adnr.flowmanager.dto.FileConversionErrorEvent;

public interface FileConversionResultConsumer {

    void consume(FileConversionCompletedEvent event);

    void consume(FileConversionErrorEvent event);
}
