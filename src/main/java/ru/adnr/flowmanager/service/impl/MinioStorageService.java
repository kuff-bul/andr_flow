package ru.adnr.flowmanager.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.config.MinioProperties;
import ru.adnr.flowmanager.exception.StorageException;
import ru.adnr.flowmanager.service.StorageService;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private static final String ORIGINAL_PREFIX = "source";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    private void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.bucket())
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.bucket())
                        .build());
            }
        } catch (Exception exception) {
            throw new StorageException("Failed to initialize MinIO bucket: " + properties.bucket(), exception);
        }
    }

    @Override
    public String uploadOriginal(UUID fileId, MultipartFile file) {
        String objectName = buildOriginalObjectName(fileId, file);
        String contentType = file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1L)
                    .contentType(contentType)
                    .build());
            return objectName;
        } catch (Exception exception) {
            throw new StorageException("Failed to upload original file to MinIO: " + objectName, exception);
        }
    }

    @Override
    public InputStream download(String bucket, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception exception) {
            throw new StorageException("Failed to download file from MinIO: " + bucket + "/" + objectName, exception);
        }
    }

    @Override
    public boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (ErrorResponseException exception) {
            if ("NoSuchKey".equals(exception.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check file existence in MinIO: " + objectName, exception);
        } catch (Exception exception) {
            throw new StorageException("Failed to check file existence in MinIO: " + objectName, exception);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .build());
        } catch (Exception exception) {
            throw new StorageException("Failed to delete file from MinIO: " + objectName, exception);
        }
    }

    private String buildOriginalObjectName(UUID fileId, MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename());
        originalFileName = originalFileName.replace('\\', '/');
        int lastSeparatorIndex = originalFileName.lastIndexOf('/');
        if (lastSeparatorIndex >= 0) {
            originalFileName = originalFileName.substring(lastSeparatorIndex + 1);
        }
        if (!StringUtils.hasText(originalFileName) || ".".equals(originalFileName) || "..".equals(originalFileName)) {
            originalFileName = "file";
        }
        return ORIGINAL_PREFIX + "/" + fileId + "/" + originalFileName;
    }
}
