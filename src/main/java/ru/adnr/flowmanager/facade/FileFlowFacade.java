package ru.adnr.flowmanager.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.exception.EmptyFileException;
import ru.adnr.flowmanager.exception.MissingUserLoginException;
import ru.adnr.flowmanager.service.FileFlowService;
import ru.adnr.flowmanager.subscription.SubscriptionValidationService;

@Component
@RequiredArgsConstructor
public class FileFlowFacade {

    private final FileFlowService fileFlowService;
    private final SubscriptionValidationService subscriptionValidationService;

    public FileUploadResponse upload(MultipartFile file, String userLogin) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        if (!StringUtils.hasText(userLogin)) {
            throw new MissingUserLoginException();
        }

        subscriptionValidationService.validateUploadAllowed(userLogin, file.getSize());
        return fileFlowService.upload(file);
    }

    public FileStatusResponse getStatus(UUID id) {
        return fileFlowService.getStatus(id);
    }

    public ConvertedFile downloadConvertedFile(UUID id) {
        return fileFlowService.downloadConvertedFile(id);
    }
}
