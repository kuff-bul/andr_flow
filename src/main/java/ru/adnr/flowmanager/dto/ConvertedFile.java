package ru.adnr.flowmanager.dto;

import org.springframework.core.io.InputStreamResource;

public record ConvertedFile(
        String fileName,
        String contentType,
        InputStreamResource resource
) {
}
