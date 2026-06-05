package ru.adnr.flowmanager.service;

import java.io.InputStream;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadOriginal(UUID fileId, MultipartFile file);

    InputStream download(String objectName);

    boolean exists(String objectName);
}
