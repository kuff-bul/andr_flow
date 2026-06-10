package ru.adnr.flowmanager.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.adnr.flowmanager.entity.FileStatus;
import ru.adnr.flowmanager.entity.FileTask;
import ru.adnr.flowmanager.exception.FileNotFoundException;
import ru.adnr.flowmanager.repository.FileTaskRepository;
import ru.adnr.flowmanager.service.FileTaskService;

@Service
@RequiredArgsConstructor
public class FileTaskServiceImpl implements FileTaskService {

    private final FileTaskRepository fileTaskRepository;

    @Override
    @Transactional
    public FileTask createProcessingTask(String originalFileName, String originalMinioPath) {
        FileTask fileTask = new FileTask(originalFileName, originalMinioPath);
        return fileTaskRepository.save(fileTask);
    }

    @Override
    @Transactional(readOnly = true)
    public FileTask findById(UUID id) {
        return fileTaskRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));
    }

    @Override
    @Transactional
    public FileTask markSuccess(UUID id, String convertedMinioBucket, String convertedMinioPath) {
        FileTask fileTask = findById(id);
        fileTask.setStatus(FileStatus.SUCCESS);
        fileTask.setConvertedMinioBucket(convertedMinioBucket);
        fileTask.setConvertedMinioPath(convertedMinioPath);
        fileTask.setErrorMessage(null);
        return fileTask;
    }

    @Override
    @Transactional
    public FileTask markError(UUID id, String errorMessage) {
        FileTask fileTask = findById(id);
        fileTask.setStatus(FileStatus.ERROR);
        fileTask.setErrorMessage(errorMessage);
        return fileTask;
    }
}
