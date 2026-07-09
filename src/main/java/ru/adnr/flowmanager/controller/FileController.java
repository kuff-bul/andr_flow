package ru.adnr.flowmanager.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.facade.FileFlowFacade;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileFlowFacade fileFlowFacade;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User-Login", required = false) String userLogin
    ) {
        return fileFlowFacade.upload(file, userLogin);
    }

    @GetMapping("/{id}/status")
    public FileStatusResponse getStatus(@PathVariable UUID id) {
        return fileFlowFacade.getStatus(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) {
        ConvertedFile convertedFile = fileFlowFacade.downloadConvertedFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(convertedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(convertedFile.fileName())
                        .build()
                        .toString())
                .body(convertedFile.resource());
    }
}
