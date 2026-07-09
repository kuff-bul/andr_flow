package ru.adnr.flowmanager.exception;

public class FileSizeLimitExceededException extends RuntimeException {

    public FileSizeLimitExceededException(String login, long fileSizeBytes, long maxAllowedBytes) {
        super("File size exceeds subscription limit. login=" + login
                + ", fileSizeBytes=" + fileSizeBytes
                + ", maxAllowedBytes=" + maxAllowedBytes);
    }
}
