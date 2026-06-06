package ru.adnr.flowmanager.service;

import java.util.UUID;
import ru.adnr.flowmanager.entity.FileTask;

public interface FileTaskService {

    FileTask createProcessingTask(UUID id, String originalFileName, String originalMinioPath);

    FileTask findById(UUID id);

    FileTask markSuccess(UUID id, String convertedMinioBucket, String convertedMinioPath);

    FileTask markError(UUID id, String errorMessage);
}
