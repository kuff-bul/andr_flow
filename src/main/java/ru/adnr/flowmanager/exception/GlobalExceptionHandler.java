package ru.adnr.flowmanager.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.adnr.flowmanager.dto.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ErrorResponse> handleEmptyFile(EmptyFileException exception) {
        return build(HttpStatus.BAD_REQUEST, "EMPTY_FILE", exception.getMessage(), null);
    }

    @ExceptionHandler(MissingUserLoginException.class)
    public ResponseEntity<ErrorResponse> handleMissingUserLogin(MissingUserLoginException exception) {
        return build(HttpStatus.BAD_REQUEST, "MISSING_USER_LOGIN", exception.getMessage(), null);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeLimitExceeded(FileSizeLimitExceededException exception) {
        return build(HttpStatus.FORBIDDEN, "FILE_SIZE_LIMIT_EXCEEDED", exception.getMessage(), null);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFound(SubscriptionNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, "SUBSCRIPTION_NOT_FOUND", exception.getMessage(), null);
    }

    @ExceptionHandler(SubscriptionCheckException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionCheck(SubscriptionCheckException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "SUBSCRIPTION_CHECK_FAILED", exception.getMessage(), exception);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", exception.getMessage(), null);
    }

    @ExceptionHandler(FileNotReadyException.class)
    public ResponseEntity<ErrorResponse> handleFileNotReady(FileNotReadyException exception) {
        return build(HttpStatus.CONFLICT, "FILE_NOT_READY", exception.getMessage(), null);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", "Storage operation failed", exception);
    }

    @ExceptionHandler(KafkaPublishException.class)
    public ResponseEntity<ErrorResponse> handleKafka(KafkaPublishException exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "KAFKA_PUBLISH_ERROR", "Kafka operation failed", exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error", exception);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, Exception exception) {
        if (exception != null) {
            log.error("{}: {}", code, message, exception);
        }
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }
}
