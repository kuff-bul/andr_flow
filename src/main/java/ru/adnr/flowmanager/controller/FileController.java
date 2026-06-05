package ru.adnr.flowmanager.controller;

import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.service.FileFlowService;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileFlowService fileFlowService;

    public FileController(FileFlowService fileFlowService) {
        this.fileFlowService = fileFlowService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(@RequestPart("file") MultipartFile file) {
        return fileFlowService.upload(file);
    }

    @GetMapping("/{id}/status")
    public FileStatusResponse getStatus(@PathVariable UUID id) {
        return fileFlowService.getStatus(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable UUID id) {
        ConvertedFile convertedFile = fileFlowService.downloadConvertedFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(convertedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(convertedFile.fileName())
                        .build()
                        .toString())
                .body(convertedFile.resource());
    }
}
