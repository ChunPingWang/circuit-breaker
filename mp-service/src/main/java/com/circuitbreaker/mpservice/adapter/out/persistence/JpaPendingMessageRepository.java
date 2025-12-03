package com.circuitbreaker.mpservice.adapter.out.persistence;

import com.circuitbreaker.mpservice.domain.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPendingMessageRepository extends JpaRepository<PendingMessageEntity, UUID> {

    List<PendingMessageEntity> findByStatusOrderByCreatedAtAsc(MessageStatus status);

    int countByStatus(MessageStatus status);
}
