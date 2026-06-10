package ru.adnr.flowmanager.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
