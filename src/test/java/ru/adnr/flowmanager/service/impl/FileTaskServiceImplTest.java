package ru.adnr.flowmanager.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.adnr.flowmanager.entity.FileStatus;
import ru.adnr.flowmanager.entity.FileTask;
import ru.adnr.flowmanager.exception.FileNotFoundException;
import ru.adnr.flowmanager.repository.FileTaskRepository;

@ExtendWith(MockitoExtension.class)
class FileTaskServiceImplTest {

    @Mock
    private FileTaskRepository fileTaskRepository;

    private FileTaskServiceImpl fileTaskService;

    @BeforeEach
    void setUp() {
        fileTaskService = new FileTaskServiceImpl(fileTaskRepository);
    }

    @Test
    void createProcessingTask_savesTaskWithProcessingStatus() {
        UUID id = UUID.randomUUID();
        when(fileTaskRepository.save(any(FileTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileTask fileTask = fileTaskService.createProcessingTask(id, "test.txt", "source/test.txt");

        assertThat(fileTask.getId()).isEqualTo(id);
        assertThat(fileTask.getOriginalFileName()).isEqualTo("test.txt");
        assertThat(fileTask.getOriginalMinioPath()).isEqualTo("source/test.txt");
        assertThat(fileTask.getStatus()).isEqualTo(FileStatus.PROCESSING);
        verify(fileTaskRepository).save(any(FileTask.class));
    }

    @Test
    void findById_returnsTaskWhenPresent() {
        UUID id = UUID.randomUUID();
        FileTask fileTask = new FileTask(id, "test.txt", "source/test.txt");
        when(fileTaskRepository.findById(id)).thenReturn(Optional.of(fileTask));

        FileTask result = fileTaskService.findById(id);

        assertThat(result).isSameAs(fileTask);
    }

    @Test
    void findById_throwsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(fileTaskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileTaskService.findById(id))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File task not found");
    }

    @Test
    void markSuccess_updatesStatusAndConvertedPath() {
        UUID id = UUID.randomUUID();
        FileTask fileTask = new FileTask(id, "test.txt", "source/test.txt");
        when(fileTaskRepository.findById(id)).thenReturn(Optional.of(fileTask));

        FileTask result = fileTaskService.markSuccess(id, "files", "converted/test.pdf");

        assertThat(result.getStatus()).isEqualTo(FileStatus.SUCCESS);
        assertThat(result.getConvertedMinioBucket()).isEqualTo("files");
        assertThat(result.getConvertedMinioPath()).isEqualTo("converted/test.pdf");
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void markError_updatesStatusAndErrorMessage() {
        UUID id = UUID.randomUUID();
        FileTask fileTask = new FileTask(id, "test.txt", "source/test.txt");
        when(fileTaskRepository.findById(id)).thenReturn(Optional.of(fileTask));

        FileTask result = fileTaskService.markError(id, "Conversion failed");

        assertThat(result.getStatus()).isEqualTo(FileStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("Conversion failed");
    }
}
