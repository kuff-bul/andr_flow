package ru.adnr.flowmanager.service;

import java.time.Instant;
import java.util.List;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.entity.OutboxMessage;

public interface OutboxService {

    void enqueueFileConversionRequested(FileConversionRequestedEvent event);

    List<OutboxMessage> findReadyForPublishing(Instant now, int batchSize);
}
