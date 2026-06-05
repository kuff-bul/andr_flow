package ru.adnr.flowmanager.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.adnr.flowmanager.entity.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    @Query(value = """
            select *
            from outbox_messages
            where status = 'PENDING'
              and next_attempt_at <= :now
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OutboxMessage> findReadyForPublishing(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );
}
