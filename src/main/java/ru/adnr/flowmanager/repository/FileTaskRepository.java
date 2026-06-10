package ru.adnr.flowmanager.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.adnr.flowmanager.entity.FileTask;

public interface FileTaskRepository extends JpaRepository<FileTask, UUID> {
}
