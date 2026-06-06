package ru.adnr.flowmanager.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import ru.adnr.flowmanager.config.MinioProperties;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileConversionRequestedEvent;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.entity.FileStatus;
import ru.adnr.flowmanager.entity.FileTask;
import ru.adnr.flowmanager.exception.FileNotFoundException;
import ru.adnr.flowmanager.exception.FileNotReadyException;
import ru.adnr.flowmanager.service.FileTaskService;
import ru.adnr.flowmanager.service.OutboxService;
import ru.adnr.flowmanager.service.StorageService;

@ExtendWith(MockitoExtension.class)
class FileFlowServiceImplTest {

    @Mock
    private StorageService storageService;

    @Mock
    private FileTaskService fileTaskService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private TransactionTemplate transactionTemplate;

    private FileFlowServiceImpl fileFlowService;

    @BeforeEach
    void setUp() {
        fileFlowService = new FileFlowServiceImpl(
                storageService,
                fileTaskService,
                outboxService,
                minioProperties,
                transactionTemplate
        );
    }

    @Test
    void upload_savesFileCreatesTaskAndEnqueuesEvent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        when(storageService.uploadOriginal(any(UUID.class), any())).thenAnswer(invocation -> {
            UUID uploadedFileId = invocation.getArgument(0);
            return "source/" + uploadedFileId + "/test.txt";
        });
        when(minioProperties.bucket()).thenReturn("documents");
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            TransactionCallback<FileTask> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
        when(fileTaskService.createProcessingTask(any(), anyString(), anyString()))
                .thenAnswer(invocation -> new FileTask(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));

        FileUploadResponse response = fileFlowService.upload(file);
        UUID fileId = response.fileId();

        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.status()).isEqualTo(FileStatus.PROCESSING.name());
        ArgumentCaptor<UUID> fileIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(storageService).uploadOriginal(fileIdCaptor.capture(), any());
        assertThat(fileIdCaptor.getValue()).isEqualTo(fileId);

        ArgumentCaptor<FileConversionRequestedEvent> eventCaptor = ArgumentCaptor.forClass(FileConversionRequestedEvent.class);
        verify(outboxService).enqueueFileConversionRequested(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isEqualTo(new FileConversionRequestedEvent(
                fileId.toString(),
                "documents",
                "source/" + fileId + "/test.txt",
                null,
                "test.txt"
        ));
    }

    @Test
    void getStatus_throwsWhenFileMissing() {
        UUID fileId = UUID.randomUUID();
        when(fileTaskService.findById(fileId)).thenThrow(new FileNotFoundException(fileId));

        assertThatThrownBy(() -> fileFlowService.getStatus(fileId))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File task not found");
    }

    @Test
    void downloadConvertedFile_throwsWhenFileIsStillProcessing() {
        UUID fileId = UUID.randomUUID();
        when(fileTaskService.findById(fileId)).thenReturn(new FileTask(fileId, "test.txt", "source/test.txt"));

        assertThatThrownBy(() -> fileFlowService.downloadConvertedFile(fileId))
                .isInstanceOf(FileNotReadyException.class)
                .hasMessage("Converted file is not ready yet");
    }

    @Test
    void downloadConvertedFile_returnsResourceWhenFileReady() {
        UUID fileId = UUID.randomUUID();
        FileTask fileTask = new FileTask(fileId, "test.txt", "source/test.txt");
        fileTask.setStatus(FileStatus.SUCCESS);
        fileTask.setConvertedMinioBucket("files");
        fileTask.setConvertedMinioPath("converted/" + fileId + "/test.pdf");
        when(fileTaskService.findById(fileId)).thenReturn(fileTask);
        when(storageService.download("files", "converted/" + fileId + "/test.pdf"))
                .thenReturn(new ByteArrayInputStream("pdf".getBytes(StandardCharsets.UTF_8)));

        ConvertedFile convertedFile = fileFlowService.downloadConvertedFile(fileId);

        assertThat(convertedFile.fileName()).isEqualTo("test.pdf");
        assertThat(convertedFile.contentType()).isEqualTo("application/pdf");
        assertThat(convertedFile.resource()).isInstanceOf(InputStreamResource.class);
    }
}
