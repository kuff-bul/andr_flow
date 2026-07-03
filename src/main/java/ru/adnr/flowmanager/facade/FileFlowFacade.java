package ru.adnr.flowmanager.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.exception.EmptyFileException;
import ru.adnr.flowmanager.service.FileFlowService;

@Component
@RequiredArgsConstructor
public class FileFlowFacade {

    private final FileFlowService fileFlowService;

    public FileUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        return fileFlowService.upload(file);
    }

    public FileStatusResponse getStatus(UUID id) {
        return fileFlowService.getStatus(id);
    }

    public ConvertedFile downloadConvertedFile(UUID id) {
        return fileFlowService.downloadConvertedFile(id);
    }
}
