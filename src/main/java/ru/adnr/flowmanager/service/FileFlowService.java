package ru.adnr.flowmanager.service;

import java.util.UUID;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.FileUploadResponse;

public interface FileFlowService {

    FileUploadResponse upload(MultipartFile file);

    FileStatusResponse getStatus(UUID id);

    ConvertedFile downloadConvertedFile(UUID fileId);
}
