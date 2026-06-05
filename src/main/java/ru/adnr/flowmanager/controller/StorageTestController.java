package ru.adnr.flowmanager.controller;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.StorageUploadTestResponse;
import ru.adnr.flowmanager.service.StorageService;

@RestController
@RequestMapping("/api/test/storage")
public class StorageTestController {

    private final StorageService storageService;

    public StorageTestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(path = "/original", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StorageUploadTestResponse uploadOriginal(@RequestPart("file") MultipartFile file) {
        UUID fileId = UUID.randomUUID();
        String objectName = storageService.uploadOriginal(fileId, file);
        return new StorageUploadTestResponse(fileId, objectName, storageService.exists(objectName));
    }
}
