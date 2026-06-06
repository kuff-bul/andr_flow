package ru.adnr.flowmanager.service.impl;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.config.MinioProperties;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.entity.FileStatus;
import ru.adnr.flowmanager.entity.FileTask;
import ru.adnr.flowmanager.exception.EmptyFileException;
import ru.adnr.flowmanager.exception.FileNotReadyException;
import ru.adnr.flowmanager.service.FileFlowService;
import ru.adnr.flowmanager.service.FileTaskService;
import ru.adnr.flowmanager.service.OutboxService;
import ru.adnr.flowmanager.service.StorageService;

@Service
public class FileFlowServiceImpl implements FileFlowService {

    private final StorageService storageService;
    private final FileTaskService fileTaskService;
    private final OutboxService outboxService;
    private final MinioProperties minioProperties;
    private final TransactionTemplate transactionTemplate;

    public FileFlowServiceImpl(
            StorageService storageService,
            FileTaskService fileTaskService,
            OutboxService outboxService,
            MinioProperties minioProperties,
            TransactionTemplate transactionTemplate
    ) {
        this.storageService = storageService;
        this.fileTaskService = fileTaskService;
        this.outboxService = outboxService;
        this.minioProperties = minioProperties;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        UUID fileId = UUID.randomUUID();
        String originalFileName = resolveOriginalFileName(file);
        String sourceObjectName = storageService.uploadOriginal(fileId, file);
        try {
            FileTask fileTask = transactionTemplate.execute(status -> {
                FileTask createdTask = fileTaskService.createProcessingTask(fileId, originalFileName, sourceObjectName);
                outboxService.enqueueFileConversionRequested(new FileConversionRequestedEvent(
                        createdTask.getId(),
                        originalFileName,
                        minioProperties.bucket(),
                        sourceObjectName
                ));
                return createdTask;
            });
            return new FileUploadResponse(fileTask.getId(), fileTask.getStatus().name());
        } catch (RuntimeException exception) {
            storageService.delete(sourceObjectName);
            throw exception;
        }
    }

    @Override
    public FileStatusResponse getStatus(UUID id) {
        FileTask fileTask = fileTaskService.findById(id);
        return new FileStatusResponse(
                fileTask.getId(),
                fileTask.getOriginalFileName(),
                fileTask.getStatus().name(),
                fileTask.getConvertedMinioPath(),
                fileTask.getErrorMessage()
        );
    }

    @Override
    public ConvertedFile downloadConvertedFile(UUID fileId) {
        FileTask fileTask = fileTaskService.findById(fileId);
        if (fileTask.getStatus() != FileStatus.SUCCESS || !StringUtils.hasText(fileTask.getConvertedMinioPath())) {
            throw new FileNotReadyException(fileId);
        }
        if (!StringUtils.hasText(fileTask.getConvertedMinioBucket())) {
            throw new FileNotReadyException(fileId);
        }

        return new ConvertedFile(
                resolveDownloadFileName(fileTask),
                MediaTypes.APPLICATION_PDF,
                new InputStreamResource(storageService.download(
                        fileTask.getConvertedMinioBucket(),
                        fileTask.getConvertedMinioPath()
                ))
        );
    }

    private String resolveOriginalFileName(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename());
        originalFileName = originalFileName.replace('\\', '/');
        int lastSeparatorIndex = originalFileName.lastIndexOf('/');
        if (lastSeparatorIndex >= 0) {
            originalFileName = originalFileName.substring(lastSeparatorIndex + 1);
        }
        if (!StringUtils.hasText(originalFileName) || ".".equals(originalFileName) || "..".equals(originalFileName)) {
            return "file";
        }
        return originalFileName;
    }

    private String resolveDownloadFileName(FileTask fileTask) {
        String originalFileName = fileTask.getOriginalFileName();
        int dotIndex = originalFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? originalFileName.substring(0, dotIndex) : originalFileName;
        return baseName + ".pdf";
    }

    private static final class MediaTypes {
        private static final String APPLICATION_PDF = "application/pdf";
    }
}
