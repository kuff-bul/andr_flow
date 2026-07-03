package ru.adnr.flowmanager.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import ru.adnr.flowmanager.dto.ConvertedFile;
import ru.adnr.flowmanager.dto.FileStatusResponse;
import ru.adnr.flowmanager.dto.FileUploadResponse;
import ru.adnr.flowmanager.exception.EmptyFileException;
import ru.adnr.flowmanager.service.FileFlowService;

@ExtendWith(MockitoExtension.class)
class FileFlowFacadeTest {

    @Mock
    private FileFlowService fileFlowService;

    private FileFlowFacade fileFlowFacade;

    @BeforeEach
    void setUp() {
        fileFlowFacade = new FileFlowFacade(fileFlowService);
    }

    @Test
    void upload_throwsWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> fileFlowFacade.upload(file))
                .isInstanceOf(EmptyFileException.class);
        verify(fileFlowService, never()).upload(file);
    }

    @Test
    void upload_delegatesValidFileToService() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        FileUploadResponse expected = new FileUploadResponse(UUID.randomUUID(), "PROCESSING");
        when(fileFlowService.upload(file)).thenReturn(expected);

        FileUploadResponse actual = fileFlowFacade.upload(file);

        assertThat(actual).isEqualTo(expected);
        verify(fileFlowService).upload(file);
    }

    @Test
    void getStatus_delegatesToService() {
        UUID fileId = UUID.randomUUID();
        FileStatusResponse expected = new FileStatusResponse(fileId, "test.txt", "PROCESSING", null, null);
        when(fileFlowService.getStatus(fileId)).thenReturn(expected);

        FileStatusResponse actual = fileFlowFacade.getStatus(fileId);

        assertThat(actual).isEqualTo(expected);
        verify(fileFlowService).getStatus(fileId);
    }

    @Test
    void downloadConvertedFile_delegatesToService() {
        UUID fileId = UUID.randomUUID();
        ConvertedFile expected = new ConvertedFile(
                "test.pdf",
                "application/pdf",
                new InputStreamResource(new ByteArrayInputStream(new byte[]{1}))
        );
        when(fileFlowService.downloadConvertedFile(fileId)).thenReturn(expected);

        ConvertedFile actual = fileFlowFacade.downloadConvertedFile(fileId);

        assertThat(actual).isEqualTo(expected);
        verify(fileFlowService).downloadConvertedFile(fileId);
    }
}
