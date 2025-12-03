package com.circuitbreaker.mpservice.adapter.out.persistence;

import com.circuitbreaker.mpservice.domain.model.MessageStatus;
import com.circuitbreaker.mpservice.domain.model.PendingMessage;
import com.circuitbreaker.mpservice.domain.port.MessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MessageRepositoryAdapter implements MessageRepository {

    private final JpaPendingMessageRepository jpaRepository;

    public MessageRepositoryAdapter(JpaPendingMessageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PendingMessage save(PendingMessage message) {
        PendingMessageEntity entity = PendingMessageEntity.fromDomain(message);
        PendingMessageEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PendingMessage> findById(UUID id) {
        return jpaRepository.findById(id).map(PendingMessageEntity::toDomain);
    }

    @Override
    public List<PendingMessage> findAllPending() {
        return jpaRepository.findByStatusOrderByCreatedAtAsc(MessageStatus.PENDING)
                .stream()
                .map(PendingMessageEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int countPending() {
        return jpaRepository.countByStatus(MessageStatus.PENDING);
    }

    @Override
    public void delete(PendingMessage message) {
        jpaRepository.deleteById(message.getId());
    }
}
